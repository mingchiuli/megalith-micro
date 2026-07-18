import { RoutesEnum, type Menu, type MenuNode, type Tab } from '@/type/entity'

export const debounce = <T extends (...args: unknown[]) => unknown>(
  fn: T,
  interval = 100
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout> | undefined

  return function (this: ThisParameterType<T>, ...args: Parameters<T>): void {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
      fn.apply(this, args)
    }, interval)
  }
}

export const diff = <T extends object>(oldArr: T[], newArr: T[]): boolean => {
  if (oldArr.length !== newArr.length) {
    return true
  }

  for (let i = 0; i < newArr.length; i++) {
    const newObj = newArr[i]!
    const oldObj = oldArr[i]!
    const newRecord = newObj as Record<string, unknown>
    const oldRecord = oldObj as Record<string, unknown>

    for (const key of Object.keys(newObj)) {
      if (key !== 'children' && newRecord[key] !== oldRecord[key]) {
        return true
      }
    }

    const newChildren = newRecord['children'] as T[]
    const oldChildren = oldRecord['children'] as T[]

    if (
      'children' in newObj &&
      'children' in oldObj &&
      Array.isArray(newChildren) &&
      Array.isArray(oldChildren)
    ) {
      if (diff(oldChildren, newChildren)) {
        return true
      }
    } else if ('children' in newObj !== 'children' in oldObj || newChildren !== oldChildren) {
      return true
    }
  }

  return false
}

const isRouteMenuNode = (node: MenuNode): node is Menu => node.type !== RoutesEnum.BUTTON

export const findMenuByPath = (menus: MenuNode[], path: string): Menu | Tab | undefined => {
  for (const node of menus) {
    if (!isRouteMenuNode(node)) {
      continue
    }
    if (node.url === path) {
      return node
    }
    const item = findMenuByPath(node.children, path)
    if (item) {
      return item
    }
  }
}

export const cleanJsonResponse = (response: string): string => {
  let cleaned = response
    .replace(/```json\n?/g, '')
    .replace(/```\n?/g, '')
    .trim()

  if (!cleaned.startsWith('{')) {
    const start = cleaned.indexOf('{')
    if (start !== -1) {
      cleaned = cleaned.slice(start)
    }
  }

  if (!cleaned.endsWith('}')) {
    const end = cleaned.lastIndexOf('}')
    if (end !== -1) {
      cleaned = cleaned.slice(0, end + 1)
    }
  }

  return cleaned
}
