rootProject.name = "megalith-micro"
springboot.version = "3.3.2"
redisson.version = "3.35.0"
caffeine.version = "3.1.8"


include("micro-gateway")
include("micro-auth")
include("micro-blog")
include("micro-user")
include("micro-search")
include("micro-websocket")
include("micro-exhibit")