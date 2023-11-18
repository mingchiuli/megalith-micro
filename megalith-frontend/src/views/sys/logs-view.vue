<script lang="ts" setup>
import { onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

let timer: NodeJS.Timeout

const client = new Client({
  brokerURL: `${import.meta.env.VITE_BASE_WS_URL}/log`,
  connectHeaders: { "Authorization": localStorage.getItem('accessToken')!, "Type": "Log" },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
})

const connect = () => {
  client.onConnect = _frame => {
    const parent = document.getElementById('parent')!
    client.subscribe('/logs/log', res => {
      let str = res.body
      let p = document.createElement('p')
      const log = document.createTextNode(res.body)
      p.appendChild(log)
      parent.appendChild(p)
      if (str.includes('INFO')) {
        p.style.color = 'green'
      } else if (str.includes('ERROR')) {
        p.style.color = 'darkred'
      } else {
        p.style.color = 'orange'
      }
    })
  }
  client.activate()

  client.onStompError = frame => {
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
    <div id="parent" class="text-item"></div>
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
  min-height: 450px;
  overflow: auto;
}
</style>