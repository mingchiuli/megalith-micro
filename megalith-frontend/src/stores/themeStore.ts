/**
 * Theme store - dark/light theme with system preference detection
 */
export const themeStore = defineStore('themeStore', () => {
  const getSystemTheme = (): boolean => {
    if (typeof globalThis.matchMedia === 'function') {
      const prefersDark = globalThis.matchMedia('(prefers-color-scheme: dark)')
      return prefersDark.matches
    }
    // Default to light mode
    return false
  }

  const isDark = ref(false)

  const applyTheme = () => {
    if (typeof document === 'undefined') return
    document.documentElement.classList.toggle('dark', isDark.value)
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
    applyTheme()
    document.cookie = `megalith_theme=${isDark.value ? 'dark' : 'light'}; Path=/; SameSite=Lax`
  }

  // Initialize theme on app start
  const initTheme = () => {
    if (typeof document === 'undefined') return
    const persistedTheme = document.cookie.match(
      /(?:^|;\s*)megalith_theme=(dark|light)(?:;|$)/
    )?.[1]
    if (persistedTheme) {
      isDark.value = persistedTheme === 'dark'
    } else {
      isDark.value = getSystemTheme()
    }
    applyTheme()
  }

  return { isDark, toggleTheme, initTheme }
})
