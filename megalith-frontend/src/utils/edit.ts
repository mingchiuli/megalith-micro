import {
  ActionType,
  FieldName,
  FieldType,
  OperateTypeCode,
  ParaInfo,
  type EditForm,
  type OperateStatusParam,
  type PushActionForm
} from '@/type/entity'
import { watch } from 'vue'
import { dealAction, pushActionData, pushAllData, recheckSensitive } from './editUtils'

export const watchInput = (
  form: EditForm,
  pushActionForm: PushActionForm,
  operateStatus: OperateStatusParam
) => {
  watch(
    () => form.description,
    (n, o) => {
      if (!preCheck(n, o, operateStatus)) return
      commonPreDeal(FieldType.NON_PARA, FieldName.DESCRIPTION, form, pushActionForm, operateStatus)
      deal(n, o, operateStatus, pushActionForm, form)
      recheckSensitive(pushActionForm, form, pushActionForm.indexStart!)
    }
  )

  watch(
    () => form.status,
    (n, o) => {
      if (!operateStatus.client.OPEN || (!n && !o) || operateStatus.composing) return
      commonPreDeal(FieldType.NON_PARA, FieldName.STATUS, form, pushActionForm, operateStatus)
      pushActionForm.operateTypeCode = OperateTypeCode.STATUS
      pushActionForm.contentChange = String(form.status)
      pushActionData(pushActionForm, operateStatus, form)
    }
  )

  watch(
    () => form.sensitiveContentList,
    (n, o) => {
      if (
        !operateStatus.client.OPEN ||
        (o.length === 0 && n.length === 0) ||
        operateStatus.composing
      )
        return
      commonPreDeal(
        FieldType.NON_PARA,
        FieldName.SENSITIVE_CONTENT_LIST,
        form,
        pushActionForm,
        operateStatus
      )
      pushActionForm.operateTypeCode = OperateTypeCode.SENSITIVE_CONTENT_LIST
      pushActionForm.contentChange = JSON.stringify(form.sensitiveContentList)
      pushActionData(pushActionForm, operateStatus, form)
    },
    { deep: true }
  )

  watch(
    () => form.link,
    (n, o) => {
      if (!preCheck(n, o, operateStatus)) return
      commonPreDeal(FieldType.NON_PARA, FieldName.LINK, form, pushActionForm, operateStatus)
      deal(n, o, operateStatus, pushActionForm, form)
    }
  )

  watch(
    () => form.title,
    (n, o) => {
      if (!preCheck(n, o, operateStatus)) return
      commonPreDeal(FieldType.NON_PARA, FieldName.TITLE, form, pushActionForm, operateStatus)
      deal(n, o, operateStatus, pushActionForm, form)
      recheckSensitive(pushActionForm, form, pushActionForm.indexStart!)
    }
  )

  watch(
    () => form.content,
    (n, o) => {
      if (!preCheck(n, o, operateStatus)) return
      commonPreDeal(FieldType.PARA, FieldName.CONTENT, form, pushActionForm, operateStatus)

      const nArr = n!.split(ParaInfo.PARA_SPLIT)
      const oArr = o!.split(ParaInfo.PARA_SPLIT)

      //本段内操作
      if (nArr.length === oArr.length) {
        let idxStart = 0
        for (let i = 0; i < nArr.length; i++) {
          if (nArr[i] !== oArr[i]) {
            pushActionForm.paraNo = i + 1
            deal(nArr[i], oArr[i], operateStatus, pushActionForm, form)
            recheckSensitive(pushActionForm, form, idxStart + pushActionForm.indexStart!)
          }
          idxStart += nArr[i].length
          idxStart += ParaInfo.PARA_SPLIT.length
        }
        return
      }
      //向后新增段
      const nLen = nArr.length
      const oLen = oArr.length
      if (nLen - 1 === oLen && nArr[oLen - 1] + '\n' === oArr[oLen - 1] && nArr[oLen] === '') {
        pushActionForm.paraNo = nLen
        pushActionForm.operateTypeCode = OperateTypeCode.PARA_SPLIT_APPEND
        pushActionData(pushActionForm, operateStatus, form)
        return
      }

      //向前减少段
      if (nLen + 1 === oLen && nArr[nLen - 1] === oArr[nLen - 1] + '\n' && oArr[nLen] === '') {
        pushActionForm.paraNo = oLen
        pushActionForm.operateTypeCode = OperateTypeCode.PARA_SPLIT_SUBTRACT
        pushActionData(pushActionForm, operateStatus, form)
        return
      }

      //推全量
      pushAllData(operateStatus, form)
      form.sensitiveContentList = []
    }
  )
}

const preCheck = (
  n: string | undefined,
  o: string | undefined,
  operateStatus: OperateStatusParam
): boolean => {
  if (o === undefined || n === undefined) {
    return false
  }

  if (n === o) {
    return false
  }

  if (operateStatus.composing || operateStatus.pulling) {
    return false
  }

  return true
}

const commonPreDeal = (
  fieldTypeParam: FieldType,
  OperateField: FieldName,
  form: EditForm,
  pushActionForm: PushActionForm,
  operateStatus: OperateStatusParam
) => {
  clearPushActionForm(pushActionForm)
  pushActionForm.field = OperateField
  pushActionForm.id = form.id
  pushActionForm.version = ++form.version
  operateStatus.fieldType = fieldTypeParam
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
  operateStatus: OperateStatusParam,
  pushActionForm: PushActionForm,
  form: EditForm
) => {
  const type = dealAction(n, o, pushActionForm, operateStatus.fieldType)
  if (ActionType.PUSH_ACTION === type) {
    pushActionData(pushActionForm, operateStatus, form)
    return
  }

  if (ActionType.PULL_ALL === type) {
    pushAllData(operateStatus, form)
  }
}
