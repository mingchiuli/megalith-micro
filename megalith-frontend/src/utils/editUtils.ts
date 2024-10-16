import { GET, POST } from '@/http/http'
import {
  ActionType,
  FieldName,
  FieldType,
  OperaColor,
  OperateTypeCode,
  SensitiveType,
  Status,
  type BlogEdit,
  type EditForm,
  type OpreateStatusParam,
  type PushActionForm
} from '@/type/entity'

export const pushAllData = async (opreateStatus: OpreateStatusParam, form: EditForm) => {
  form.version++
  await POST<null>('/sys/blog/edit/push/all', form)
  if (opreateStatus.transColor.value !== OperaColor.WARNING) {
    opreateStatus.transColor.value = OperaColor.WARNING
  }
}

export const pushActionData = (
  pushActionForm: PushActionForm,
  opreateStatus: OpreateStatusParam,
  form: EditForm
) => {
  pushActionForm.version = ++form.version
  opreateStatus.client.send(JSON.stringify(pushActionForm))
  if (opreateStatus.transColor.value !== OperaColor.SUCCESS) {
    opreateStatus.transColor.value = OperaColor.SUCCESS
  }
}

export const pullAllData = async (opreateStatus: OpreateStatusParam, form: EditForm) => {
  await loadEditContent(form, opreateStatus)
}

export const loadEditContent = async (form: EditForm, opreateStatus: OpreateStatusParam) => {
  let data
  if (!opreateStatus.blogId) {
    data = await GET<BlogEdit>('/sys/blog/edit/pull/echo')
  } else {
    data = await GET<BlogEdit>(`/sys/blog/edit/pull/echo?blogId=${opreateStatus.blogId}`)
  }
  form.title = data.title
  form.description = data.description
  form.content = data.content
  form.link = data.link
  form.status = data.status
  form.id = data.id
  form.userId = data.userId
  form.sensitiveContentList = data.sensitiveContentList
  form.version = data.version
}

export const dealAction = (
  n: string | undefined,
  o: string | undefined,
  pushActionForm: PushActionForm,
  fieldType: FieldType
): ActionType => {
  //全部删除
  const para = fieldType === FieldType.PARA
  if (!n) {
    pushActionForm.operateTypeCode = para
      ? OperateTypeCode.PARA_REMOVE
      : OperateTypeCode.NON_PARA_REMOVE
    return ActionType.PUSH_ACTION
  }

  //初始化新增
  if (!o) {
    pushActionForm.contentChange = n
    pushActionForm.operateTypeCode = para
      ? OperateTypeCode.PARA_TAIL_APPEND
      : OperateTypeCode.NON_PARA_TAIL_APPEND
    return ActionType.PUSH_ACTION
  }

  const nLen = n.length
  const oLen = o.length
  const minLen = Math.min(nLen, oLen)

  let indexStart = oLen
  for (let i = 0; i < minLen; i++) {
    if (n.charAt(i) !== o.charAt(i)) {
      indexStart = i
      break
    }
  }

  if (indexStart === oLen) {
    //向末尾添加
    if (oLen < nLen) {
      pushActionForm.contentChange = n.substring(indexStart)
      pushActionForm.operateTypeCode = para
        ? OperateTypeCode.PARA_TAIL_APPEND
        : OperateTypeCode.NON_PARA_TAIL_APPEND
    } else {
      //从末尾删除
      pushActionForm.indexStart = nLen
      pushActionForm.operateTypeCode = para
        ? OperateTypeCode.PARA_TAIL_SUBTRACT
        : OperateTypeCode.NON_PARA_TAIL_SUBTRACT
    }
    return ActionType.PUSH_ACTION
  }

  let oIndexEnd = -1
  let nIndexEnd = -1
  for (let i = oLen - 1, j = nLen - 1; i >= 0 && j >= 0; i--, j--) {
    if (o.charAt(i) !== n.charAt(j)) {
      oIndexEnd = i + 1
      nIndexEnd = j + 1
      break
    }
  }

  if (oIndexEnd === -1) {
    //从开头添加
    if (oLen < nLen) {
      pushActionForm.contentChange = n.substring(0, nLen - oLen)
      pushActionForm.operateTypeCode = para
        ? OperateTypeCode.PARA_HEAD_APPEND
        : OperateTypeCode.NON_PARA_HEAD_APPEND
    } else {
      //从开头删除
      pushActionForm.indexStart = oLen - nLen
      pushActionForm.operateTypeCode = para
        ? OperateTypeCode.PARA_HEAD_SUBTRACT
        : OperateTypeCode.NON_PARA_HEAD_SUBTRACT
    }
    return ActionType.PUSH_ACTION
  }

  //中间操作重复字符
  if (indexStart > oIndexEnd) {
    let contentChange
    if (nIndexEnd > oIndexEnd) {
      //增
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = indexStart
      contentChange = n.substring(indexStart, nIndexEnd + (indexStart - oIndexEnd))
    } else {
      //删
      contentChange = ''
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = indexStart + oIndexEnd - nIndexEnd
    }
    pushActionForm.contentChange = contentChange
    pushActionForm.operateTypeCode = para
      ? OperateTypeCode.PARA_REPLACE
      : OperateTypeCode.NON_PARA_REPLACE
    return ActionType.PUSH_ACTION
  }

  //中间正常插入/删除
  if (indexStart <= oIndexEnd) {
    let contentChange
    if (indexStart < nIndexEnd) {
      contentChange = n.substring(indexStart, nIndexEnd)
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = oIndexEnd
    } else {
      contentChange = ''
      pushActionForm.indexStart = indexStart
      pushActionForm.indexEnd = indexStart + (oIndexEnd - nIndexEnd)
    }

    pushActionForm.contentChange = contentChange
    pushActionForm.operateTypeCode = para
      ? OperateTypeCode.PARA_REPLACE
      : OperateTypeCode.NON_PARA_REPLACE
    return ActionType.PUSH_ACTION
  }
  //全不满足直接推全量数据
  return ActionType.PUSH_ALL
}

export const recheckSensitive = (pushActionForm: PushActionForm, form: EditForm) => {
  if (form.status !== Status.SENSITIVE_FILTER) {
    return
  }
  const field = pushActionForm.field
  const operateType = pushActionForm.operateTypeCode
  const len = form.sensitiveContentList.length
  const indexStart = pushActionForm.indexStart!
  /**
   * type 1
   */

  if (
    field === FieldName.TITLE &&
    (operateType === OperateTypeCode.NON_PARA_REMOVE ||
      operateType === OperateTypeCode.NON_PARA_HEAD_APPEND ||
      operateType === OperateTypeCode.NON_PARA_HEAD_SUBTRACT)
  ) {
    const sensitiveList = form.sensitiveContentList.filter(
      (item) => item.type !== SensitiveType.TITLE
    )
    if (len !== sensitiveList.length) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (
    field === FieldName.DESCRIPTION &&
    (operateType === OperateTypeCode.NON_PARA_REMOVE ||
      operateType === OperateTypeCode.NON_PARA_HEAD_APPEND ||
      operateType === OperateTypeCode.NON_PARA_HEAD_SUBTRACT)
  ) {
    const sensitiveList = form.sensitiveContentList.filter(
      (item) => item.type !== SensitiveType.DESCRIPTION
    )
    if (len !== sensitiveList.length) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (
    field === FieldName.CONTENT &&
    (operateType === OperateTypeCode.PARA_REMOVE ||
      operateType === OperateTypeCode.PARA_HEAD_APPEND ||
      operateType === OperateTypeCode.PARA_HEAD_SUBTRACT)
  ) {
    const sensitiveList = form.sensitiveContentList.filter(
      (item) => item.type !== SensitiveType.CONTENT
    )
    if (len !== sensitiveList.length) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  /**
   * type 2
   */

  if (
    field === FieldName.TITLE &&
    (operateType === OperateTypeCode.NON_PARA_TAIL_SUBTRACT ||
      operateType === OperateTypeCode.NON_PARA_REPLACE)
  ) {
    const sensitiveList = form.sensitiveContentList.filter(
      (item) => item.type !== SensitiveType.TITLE || item.endIndex - 1 < indexStart
    )
    if (sensitiveList.length !== len) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (
    field === FieldName.DESCRIPTION &&
    (operateType === OperateTypeCode.NON_PARA_TAIL_SUBTRACT ||
      operateType === OperateTypeCode.NON_PARA_REPLACE)
  ) {
    const sensitiveList = form.sensitiveContentList.filter(
      (item) => item.type !== SensitiveType.DESCRIPTION || item.endIndex - 1 < indexStart
    )
    if (sensitiveList.length !== len) {
      form.sensitiveContentList = sensitiveList
    }
    return
  }

  if (
    field === FieldName.CONTENT &&
    (operateType === OperateTypeCode.PARA_TAIL_SUBTRACT ||
      operateType === OperateTypeCode.PARA_REPLACE)
  ) {
    const sensitiveList = form.sensitiveContentList.filter(
      (item) => item.type !== SensitiveType.CONTENT || item.endIndex - 1 < indexStart
    )
    if (sensitiveList.length !== len) {
      form.sensitiveContentList = sensitiveList
    }
  }
}
