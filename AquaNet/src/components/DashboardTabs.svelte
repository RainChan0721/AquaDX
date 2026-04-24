<script lang="ts">
    import { t } from "../libs/i18n";

    const tabs: Record<string, string> = {
        [t('home.nav.portal')]: `/home`,
        [t('home.nav.link-card')]: `/cards`,
        [t('home.nav.game-setup')]: `/setup`,
        [t('home.nav.support')]: `/support`
    }
</script>

<nav class="tabs">
    {#each Object.entries(tabs) as t, i}
        <a class="clickable"
            href={t[1]}
            class:active={
                new URL(location.href).pathname === t[1]
            }>
            {t[0]}
        </a>
    {/each}
</nav>

<style lang="sass">
  @use "../vars"
  .tabs
    display: flex
    gap: 1rem

    a
      &.active
        color: vars.$c-main
</style>