
/**
 * Theme store - dark/light theme with system preference detection
 */
export const themeStore = defineStore('themeStore', () => {
  // Get system theme preference
  const getSystemTheme = (): boolean => {
    if (window.matchMedia) {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)')
      return prefersDark.matches
    }
    // Default to light mode
    return false
  }

  const isDark = ref(getSystemTheme())

  const toggleTheme = () => {
    isDark.value = !isDark.value
    const htmlElement = document.documentElement

    if (isDark.value) {
      htmlElement.classList.add('dark')
    } else {
      htmlElement.classList.remove('dark')
    }
  }

  // Initialize theme on app start
  const initTheme = () => {
    const htmlElement = document.documentElement
    if (isDark.value) {
      htmlElement.classList.add('dark')
    } else {
      htmlElement.classList.remove('dark')
    }
  }

  return { isDark, toggleTheme, initTheme }
})