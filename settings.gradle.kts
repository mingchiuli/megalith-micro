rootProject.name = "megalith-micro"
ext.springboot.version = "3.3.2"
ext.edisson.version = "3.35.0"
ext.caffeine.version = "3.1.8"


include("micro-gateway")
include("micro-auth")
include("micro-blog")
include("micro-user")
include("micro-search")
include("micro-websocket")
include("micro-exhibit")