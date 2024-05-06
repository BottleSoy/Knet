/**
 * 服务端配置类
 */
class ServerConfig {
    sections: SectionConfig[]
}
/**
 * 协议选择器
 * 对于{@link String}类型使用的是直接选择
 * 对于{@link String[]}使用的是
 */
type Selector = String | String[] | ((Connection)=>String)

/**
 * 一个区间配置
 * 
 * 通常来说一个区间配置对应的是一个协议
 * 
 * 对于部分区间配置可能会连带一串协议
 */
interface SectionConfig {
    type: String
    name: String
}

/**
 * 反向代理协议
 */
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

    /**
     * 
     */
    contexts: SSLContext[] | Function
    contextSeletor: String | Function | undefined
    

    configure: {
        tlsVersion: String[] | String  | undefined
        alpn: String[] | String  | undefined
        chipers: String[] | String  | undefined
    } | Function | undefined



    selector: Selector | {
        /**
         * 键值对形式:  `<domain-name>: <Protocol-Name>`
         */
        domains: {} | null
        fallback: String | null
    }
}