import axios from "axios";
import { ElMessage } from 'element-plus'

axios.defaults.baseURL = 'http://127.0.0.1:8081'
axios.defaults.timeout = 10000

axios.interceptors.request.use(config => {
  return config
})

axios.interceptors.response.use(resp => {
  const data : {"status" : number, "msg": string, "data" : object} = resp.data
  if (data.status === 200) {
    return Promise.resolve(resp.data.data)
  } else {    
    if (data.status === 401) {
      // TODO
    }
    ElMessage.error(data.msg)
    return Promise.reject(data.msg)
  }
}, error => {
  ElMessage.error(error.message)
  return Promise.reject(error.message)
})

export default axios