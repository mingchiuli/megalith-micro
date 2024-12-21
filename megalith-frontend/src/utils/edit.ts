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

const createWatch = (
  field: keyof EditForm,
  fieldType: FieldType,
  operateTypeCode: OperateTypeCode | undefined,
  form: EditForm,
  pushActionForm: PushActionForm,
  operateStatus: OperateStatusParam,
  deep: boolean = false
) => {
  watch(
    () => form[field],
    (n, o) => {
      if (!preCheck(n, o, operateStatus)) return
      commonPreDeal(fieldType, FieldName[field.toUpperCase() as keyof typeof FieldName], form, pushActionForm, operateStatus)
      if (operateTypeCode !== undefined) {
        pushActionForm.operateTypeCode = operateTypeCode
        pushActionForm.contentChange = String(form[field])
        pushActionData(pushActionForm, operateStatus, form)
      } else {
        deal(n, o, operateStatus, pushActionForm, form)
        recheckSensitive(pushActionForm, form)
      }
    },
    { deep }
  )
}

export const watchInput = (
  form: EditForm,
  pushActionForm: PushActionForm,
  operateStatus: OperateStatusParam
) => {
  createWatch('description', FieldType.NON_PARA, undefined, form, pushActionForm, operateStatus)
  createWatch('status', FieldType.NON_PARA, OperateTypeCode.STATUS, form, pushActionForm, operateStatus)
  createWatch('sensitiveContentList', FieldType.NON_PARA, OperateTypeCode.SENSITIVE_CONTENT_LIST, form, pushActionForm, operateStatus, true)
  createWatch('link', FieldType.NON_PARA, undefined, form, pushActionForm, operateStatus)
  createWatch('title', FieldType.NON_PARA, undefined, form, pushActionForm, operateStatus)
  createWatch('content', FieldType.PARA, undefined, form, pushActionForm, operateStatus)
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
