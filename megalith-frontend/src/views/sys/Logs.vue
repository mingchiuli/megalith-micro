<script lang="ts" setup>
import { onUnmounted, ref } from 'vue'
import { Client } from '@stomp/stompjs';

const msg = ref('')
const loading = ref(false)
let timer: NodeJS.Timeout

const client = new Client({
  brokerURL: `${import.meta.env.VITE_BASE_WS_URL}/log`,
  connectHeaders: { "Authorization": localStorage.getItem('accessToken')!, "Type": "Log" },
  debug: function (str) {
    console.log(str)
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
})

const connect = () => {
  client.onConnect = function (_frame) {
    client.subscribe('/logs/log', res => {
      let str = res.body
      if (str.includes('INFO')) {
        msg.value += '<p style="color: green">' + res.body + '</p>'
      } else if (str.includes('ERROR')) {
        msg.value += '<p style="color: darkred">' + res.body + '</p>'
      } else {
        msg.value += '<p style="color: orange">' + res.body + '</p>'
      }
      if (!loading.value) {
        loading.value = false
      }
    })
  }
  client.activate()

  client.onStompError = function (frame) {
    ElNotification.error({
      title: 'Broker reported error: ' + frame.headers['message'],
      message: 'Additional details: ' + frame.body,
      showClose: true
    })
  }
}

const show = () => {
  client.publish({
    destination: '/app/log/start'
  })
}

const stop = () => {
  client.publish({
    destination: '/app/log/stop'
  })
}

onUnmounted(() => {
  clearInterval(timer)
  stop()
  client.deactivate()
});

(() => {
  connect()
  timer = setInterval(() => {
    if (!client.connected) {
      ElNotification.warning("websocket reconnection ...")
      connect()
    }
  }, 2000)
})()
</script>

<template>
  <el-card shadow="never">
    <div class="header">
      <span id="SLTitle">日志平台</span>
      <div id="SLContent">
        <el-button class="SLButton" link @click="show">Start</el-button>
        <el-divider direction="vertical"></el-divider>
        <el-button class="SLButton" link @click="stop">Stop</el-button>
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

.text-item {
  overflow: auto;
}
</style>