<script lang="ts">
  import { fade } from "svelte/transition";
  import { t } from "../../libs/i18n";
  import { DISCORD_INVITE } from "../../libs/config";

  export let error: string;
</script>

<div class="overlay" transition:fade>
  <div>
    <h2 class="error">{t('status.error')}</h2>
    <span class="detail">{error}</span>
    <a class="hint" href="/support">{t("status.error.hint")}</a>
    <div class="actions">
      <button on:click={() => location.reload()} class="error">
        {t('action.refresh')}
      </button>
      <button on:click={() => location.href = "/home"} class="error">
        {t('action.home')}
      </button>
    </div>
  </div>
</div>

<style lang="sass">
  .actions
    display: flex
    gap: 0.5em

    button
      width: 100%

  .detail
    white-space: pre-line
    line-height: 1.2

  .hint
    font-size: 0.875em

  .overlay > div
    min-width: 15rem
    max-width: 25rem
</style>
