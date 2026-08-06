// backported from AquaNet2

export interface NetworkData {
    keychip?: string,
    dns?: string
}

function isSection(section: string): boolean {
    return section.substring(0, 1) === "[" 
        && section.substring(section.length - 1, section.length) === "]"
}

function addOrUpdateItem(ini: string[], section: string, key: string, value: string) {
    let sections: Record<string, {
        idx: number,
        keys: Record<string, number>
    }> = {};
    let activeSection: string | undefined;

    for (let [idx, item] of ini.entries()) {
        if (isSection(item)) {
            activeSection = item.substring(1, item.length - 1)
            sections[activeSection] = {
                idx, keys: {}
            };
        } else {
            let key = item.split("=")[0];
            let value = item.split("=")[1];

            if (!key)
                continue;
            
            if (activeSection && sections[activeSection])
                sections[activeSection].keys[key] = idx;
        }
    }

    if (sections[section] && sections[section].keys[key]) {
        ini[
            sections[section].keys[key]
        ] = `${key}=${value}`;
    } else if (sections[section]) {
        ini.splice(sections[section].idx + 1, 0, `${key}=${value}`);
    } else {
        ini.push(`[${section}]`)
        ini.push(`${key}=${value}`)
    }
}

export function injectNetworkData(baseIni: string, networkData: NetworkData): string {
    let ini: string[] = baseIni.split("\n").map(i => i.trim().toLowerCase().replaceAll("\r", ""));
    
    if (networkData.dns)
        addOrUpdateItem(ini, "dns", "default", networkData.dns);
    addOrUpdateItem(ini, "keychip", "enable", "1");
    if (networkData.keychip)
        addOrUpdateItem(ini, "keychip", "id", networkData.keychip);

    return ini.join("\n");
}

export async function patchUserSegatools(networkData: NetworkData): Promise<boolean> {
    try {
        let [file] = await window.showOpenFilePicker({
            multiple: false,
            types: [{
                description: "segatools.ini",
                accept: {"text/ini": ".ini"}
            }]
        });
        let writable = await file.createWritable();
        await writable.write(
            injectNetworkData(
                await (await file.getFile()).text(),
                networkData
            )
        );
        await writable.close();
        return true;
    } catch(err) {
        return false;
    }
}
