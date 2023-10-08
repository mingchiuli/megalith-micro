<script lang="ts" setup>
import { ref } from 'vue'
import { Client } from '@stomp/stompjs'

const stompClient = new Client({
  connectHeaders: { "Authorization": localStorage.getItem('accessToken')!, "Type": 'Log' },
  debug: function (str) {
    //debug日志，调试时候开启
    // console.log(str);
  },
  reconnectDelay: 2000,//重连时间
  heartbeatIncoming: 2000,
  heartbeatOutgoing: 2000,
})

// stompClient.webSocketFactory = function () {
//   //因为服务端监听的是/sysLog路径下面的请求，所以跟服务端保持一致
//   return new SockJS(import.meta.env.VITE_BASE_URL + '/sysLog');
// };


const show = () => {

}

const stop = () => {

}

const loading = ref(false)

const msg = ref('')


</script>

<template>
  <el-card class="box-card">
    <div slot="header" class="clearfix">
      <span id="SLTitle">日志平台</span>
      <div id="SLContent">
        <el-button class="SLButton" type="text" @click="show">Start monitor</el-button>
        <el-divider direction="vertical"></el-divider>
        <el-button class="SLButton" type="text" @click="stop">Stop monitor</el-button>
      </div>
    </div>
    <div v-html="msg" class="text-item" v-loading="loading"></div>
  </el-card>
</template>

<style scoped>
#SLTitle {
  font-size: large;
}

#SLContent {
  float: right;
  padding: 0.5% 0;
}

.SLButton {
  font-size: medium;
}
</style>