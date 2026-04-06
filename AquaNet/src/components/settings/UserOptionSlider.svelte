<script lang="ts">
  import { t } from "../../libs/i18n";
  import { EN_REF } from "../../libs/i18n/en_ref";
  import type { GameName } from "../../libs/scoring";
  import { SETTING } from "../../libs/sdk";

    export let getTextFunction: (value: number) => string
    export let defaultValue: number = 0

    export let minValue: number = 0
    export let maxValue: number = 0
    export let type: string = "speed";
    export let game: GameName;

    export let disabled: boolean = false;

    let value = defaultValue;
    let rationalizedValue = defaultValue / maxValue;

    async function submit() {
        await SETTING.optionSet(game, type, value);
    }
</script>
<div class="field range">
    <div class="range-label">
        {t(`settings.options.${game}.${type}` as keyof typeof EN_REF)} <!-- TODO: i18n -->
    </div>
    <div class="range-value">
        <input 
            type="range" min={minValue} {disabled}
            on:change={submit} max={maxValue} bind:value={value} 
        >
        <div>
            {getTextFunction(value)}
        </div>
    </div>
</div>
<style lang="sass">
    @use "../../vars"

    .field
        width: 100%

        .range-value
            display: flex
            align-items: center
            
            div 
                min-width: 4rem
                white-space: nowrap
                display: flex
                justify-content: center

            input
                flex: 100%
    
    input
        -webkit-appearance: none
        width: 100%
        height: 5px !important
        outline: none
        transition: opacity 200ms
        padding: 4px
        border-radius: 4px
        opacity: 80%
        background: linear-gradient(to right, transparent 0%, #caf1 10%, #caf1 90%, transparent 100%)

        margin: 16px 0

        &:hover
            opacity: 100%

    *:focus
        outline: none

    input::-webkit-slider-thumb 
        -webkit-appearance: none
        appearance: none
        width: 8px
        height: 30px
        background: vars.$c-main
        border-radius: 8px
        cursor: pointer
        box-shadow: 0 0 2px 0 vars.$c-main
    
</style>