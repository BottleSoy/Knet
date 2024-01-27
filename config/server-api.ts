/**
 * 服务端的配置类,定义了
 */
class ServerConfig {
    sections: SectionConfig[]
}

type Selector = String | String[] | Function

interface SectionConfig {
    type: String
    name: String
}

class ForwardSectionConfig implements SectionConfig {
    type: String = "forward"
    name: String

    forward: String
}
class TcpSectionConfig implements SectionConfig {
    type: String = "tcp"
    name: String

    /**
     * TCP服务所监听的地址
     * 若address属性为函数,则函数无参,且返回值为{@link String}
     */
    address: String | Function


    selector: Selector
}



class SSLContext {
    pemFiles: String[] | Function
}


class TLSSectionConfig implements SectionConfig {
    type: String = "tls"
    name: String

    /**
     * 
     */
    context: Function | SSLContext


    contexts: SSLContext[] | Function
    "context-seletor": String | Function

    configure: {
        "tls-version": String[] | String | null
        "alpn": String[] | String | null
        "chipers": String[] | String | null
    } | Function | null



    selector: Selector | {
        /**
         * 键值对形式:  `<domain-name>: <Protocol-Name>`
         */
        domains: {} | null
        fallback: String | null
    }
}