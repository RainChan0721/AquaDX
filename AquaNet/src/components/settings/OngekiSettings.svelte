<script>
  import { fade } from "svelte/transition";
  import { FADE_IN, FADE_OUT } from "../../libs/config";
  import GameSettingFields from "./GameSettingFields.svelte";

  function exportData() {
      submitting = "export"
      GAME.export('ongeki')
        .then(data => download(JSON.stringify(data), `AquaDX_ongeki_export_${values[0]}.json`))
        .catch(e => error = e.message)
        .finally(() => submitting = "")
    }
</script>

<div>
  <GameSettingFields game="ongeki"/>
  <button class="exportButton" on:click={exportData}>
      <Icon icon="bxs:file-export"/>
      {t('settings.export')}
    </button>
</div>
