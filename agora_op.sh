# 获取所有项目
curl --basic -u 4d12d1f858d244d9adaa80738d30ce7b:5dc4ca0495564fb582ec8958c70ed6af  https://api.agora.io/dev/v1/projects/

      
{
    "projects":[
        {
            "id":"rkKbefMME",
            "name":"撕歌测试环境",
            "vendor_key":"f23bd32ce6484113b02d14bd878e694c",
            "sign_key":"b142fa8feaba4da49e5b348dd78c3416",
            "recording_server":null,
            "status":1,
            "created":1546948608688
        },
        {
            "id":"BJDekMzzV",
            "name":"撕歌",
            "vendor_key":"2cceda2dbb2d46d28cab627f30c1a6f7",
            "sign_key":"",
            "recording_server":null,
            "status":1,
            "created":1546948334857
        }
    ]
}

# 重置key
curl --basic -u 4d12d1f858d244d9adaa80738d30ce7b:5dc4ca0495564fb582ec8958c70ed6af -H "Content-Type:application/json" -X POST -d '{"id":"rkKbefMME"}' https://api.agora.io/dev/v1/reset_signkey/

# enable 禁用 改项目的 认证证书
curl --basic -u 4d12d1f858d244d9adaa80738d30ce7b:5dc4ca0495564fb582ec8958c70ed6af -H "Content-Type:application/json" -X POST -d '{"id":"rkKbefMME","enable":false}' https://api.agora.io/dev/v1/signkey/