package review.domain.model;

/**
 * 模型枚举类
 */
public enum Model {
    QWQ("qwq-32b-preview", "QwQ模型是由 Qwen 团队开发的实验性研究模型，专注于增强 AI 推理能力，尤其是数学和编程领域。"),
    TURBO("qwen-turbo", "通义千问系列速度最快、成本很低的模型，适合简单任务。"),
    ;

    // 对象属性：模型代码 和 模型描述
    private final String code;
    private final String info;

    /**
     * 构造函数
     *
     * @param code 模型代码
     * @param info 模型描述
     */
    Model(String code, String info) {
        this.code = code;
        this.info = info;
    }

    /**
     * get方法，外部类通过该方法获取枚举值的code
     *
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * get方法，外部类通过该方法获取枚举值的info
     *
     * @return
     */
    public String getInfo() {
        return info;
    }

}
