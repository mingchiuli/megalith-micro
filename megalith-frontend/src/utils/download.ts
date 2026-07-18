import { DOWNLOAD } from '@/http/http'
import type { Ref } from 'vue'

export const downloadSQLData = async (
  url: string,
  fileName: string,
  percentage: Ref<number>,
  percentageShow: Ref<boolean>
) => {
  const resp = await DOWNLOAD(url, percentage, percentageShow)
  const blob = new Blob([String(resp?.data)], {
    type: 'text/plain'
  })
  const anchor = document.createElement('a')
  const objectUrl = URL.createObjectURL(blob)
  anchor.download = `${fileName}.sql`
  anchor.style.display = 'none'
  anchor.href = objectUrl
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  URL.revokeObjectURL(objectUrl)
}
