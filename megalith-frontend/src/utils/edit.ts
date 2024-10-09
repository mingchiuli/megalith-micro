import {
  ActionType,
  FieldName,
  FieldType,
  OperateTypeCode,
  ParaInfo,
  type EditForm,
  type OpreateStatusParam,
  type PushActionForm
} from '@/type/entity'
import { watch } from 'vue'
import { dealAction, pushActionData, pushAllData, recheckSensitive } from './editUtils'

export const watchInput = (
  form: EditForm,
  pushActionForm: PushActionForm,
  opreateStatus: OpreateStatusParam
) => {
  watch(
    () => form.description,
    (n, o) => {
      if (!preCheck(n, o, opreateStatus)) return
      commonPreDeal(FieldType.NON_PARA, FieldName.DESCRIPTION, form, pushActionForm, opreateStatus)
      deal(n, o, opreateStatus, pushActionForm, form)
      recheckSensitive(pushActionForm, form)
    }
  )

  watch(
    () => form.status,
    (n, o) => {
      if (!opreateStatus.client.OPEN || (!n && !o) || opreateStatus.composing) return
      commonPreDeal(FieldType.NON_PARA, FieldName.STATUS, form, pushActionForm, opreateStatus)
      pushActionForm.operateTypeCode = OperateTypeCode.STATUS
      pushActionForm.contentChange = String(form.status)
      pushActionData(pushActionForm, opreateStatus, form)
    }
  )

  watch(
    () => form.sensitiveContentList,
    (n, o) => {
      if (
        !opreateStatus.client.OPEN ||
        (o.length === 0 && n.length === 0) ||
        opreateStatus.composing
      )
        return
      commonPreDeal(
        FieldType.NON_PARA,
        FieldName.SENSITIVE_CONTENT_LIST,
        form,
        pushActionForm,
        opreateStatus
      )
      pushActionForm.operateTypeCode = OperateTypeCode.SENSITIVE_CONTENT_LIST
      pushActionForm.contentChange = JSON.stringify(form.sensitiveContentList)
      pushActionData(pushActionForm, opreateStatus, form)
    },
    { deep: true }
  )

  watch(
    () => form.link,
    (n, o) => {
      if (!preCheck(n, o, opreateStatus)) return
      commonPreDeal(FieldType.NON_PARA, FieldName.LINK, form, pushActionForm, opreateStatus)
      deal(n, o, opreateStatus, pushActionForm, form)
    }
  )

  watch(
    () => form.title,
    (n, o) => {
      if (!preCheck(n, o, opreateStatus)) return
      commonPreDeal(FieldType.NON_PARA, FieldName.TITLE, form, pushActionForm, opreateStatus)
      deal(n, o, opreateStatus, pushActionForm, form)
      recheckSensitive(pushActionForm, form)
    }
  )

  watch(
    () => form.content,
    (n, o) => {
      if (!preCheck(n, o, opreateStatus)) return
      commonPreDeal(FieldType.PARA, FieldName.CONTENT, form, pushActionForm, opreateStatus)

      const nArr = n!.split(ParaInfo.PARA_SPLIT)
      const oArr = o!.split(ParaInfo.PARA_SPLIT)

      //本段内操作
      if (nArr.length === oArr.length) {
        for (let i = 0; i < nArr.length; i++) {
          if (nArr[i] !== oArr[i]) {
            pushActionForm.paraNo = i + 1
            deal(nArr[i], oArr[i], opreateStatus, pushActionForm, form)
            recheckSensitive(pushActionForm, form)
          }
        }
        return
      }
      //向后新增段
      const nLen = nArr.length
      const oLen = oArr.length
      if (nLen - 1 === oLen && nArr[oLen - 1] + '\n' === oArr[oLen - 1] && nArr[oLen] === '') {
        //每段必须相同，否则推全量
        for (let i = 0; i < oLen; i++) {
          if (i !== oLen - 1 && nArr[i] !== oArr[i]) {
            pushAllData(opreateStatus, form)
            return
          }
          if (i === oLen - 1 && nArr[i] + '\n' !== oArr[i]) {
            pushAllData(opreateStatus, form)
            return
          }
        }
        pushActionForm.paraNo = nLen
        pushActionForm.operateTypeCode = OperateTypeCode.PARA_SPLIT_APPEND
        pushActionData(pushActionForm, opreateStatus, form)
        return
      }

      //向前减少段
      if (nLen + 1 === oLen && nArr[nLen - 1] === oArr[nLen - 1] + '\n' && oArr[nLen] === '') {
        for (let i = 0; i < nLen; i++) {
          if (i !== nLen - 1 && nArr[i] !== oArr[i]) {
            pushAllData(opreateStatus, form)
            return
          }
          if (i === nLen - 1 && nArr[i] !== oArr[i] + '\n') {
            pushAllData(opreateStatus, form)
            return
          }
        }
        pushActionForm.paraNo = oLen
        pushActionForm.operateTypeCode = OperateTypeCode.PARA_SPLIT_SUBTRACT
        pushActionData(pushActionForm, opreateStatus, form)
        return
      }

      //推全量
      pushAllData(opreateStatus, form)
    }
  )
}

const preCheck = (
  n: string | undefined,
  o: string | undefined,
  opreateStatus: OpreateStatusParam
): boolean => {
  if (o === undefined || n === undefined) {
    return false
  }

  if (n === o) {
    return false
  }

  if (opreateStatus.composing) {
    return false
  }

  if (opreateStatus.pulling) {
    return false
  }

  if (opreateStatus.client.readyState !== WebSocket.OPEN) {
    if (!opreateStatus.netErrorEdited.value) {
      opreateStatus.netErrorEdited.value = true
    }
    return false
  }

  return true
}

const commonPreDeal = (
  fieldTypeParam: FieldType,
  opreateField: FieldName,
  form: EditForm,
  pushActionForm: PushActionForm,
  opreateStatus: OpreateStatusParam
) => {
  clearPushActionForm(pushActionForm)
  pushActionForm.field = opreateField
  pushActionForm.id = form.id
  opreateStatus.fieldType = fieldTypeParam
}

const clearPushActionForm = (pushActionForm: PushActionForm) => {
  pushActionForm.contentChange = undefined
  pushActionForm.indexEnd = undefined
  pushActionForm.indexStart = undefined
  pushActionForm.operateTypeCode = undefined
  pushActionForm.version = undefined
  pushActionForm.field = undefined
  pushActionForm.paraNo = undefined
}

const deal = (
  n: string | undefined,
  o: string | undefined,
  opreateStatus: OpreateStatusParam,
  pushActionForm: PushActionForm,
  form: EditForm
) => {
  const type = dealAction(n, o, pushActionForm, opreateStatus.fieldType)
  if (ActionType.PUSH_ACTION === type) {
    pushActionData(pushActionForm, opreateStatus, form)
    return
  }

  if (ActionType.PULL_ALL === type) {
    pushAllData(opreateStatus, form)
  }
}
