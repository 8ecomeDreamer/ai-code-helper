package com.example.aicodehelper.common.core;

import com.example.aicodehelper.common.constant.Constants;
import java.util.HashMap;

/**
 * 通用返回结果（若依风格）：code / msg / data，支持 put 扩展键
 */
public class Result extends HashMap<String, Object> {

    private static final long serialVersionUUID = 1L;

    /** 状态码键 */
    public static final String CODE_TAG = "code";

    /** 消息键 */
    public static final String MSG_TAG = "msg";

    /** 数据键 */
    public static final String DATA_TAG = "data";

    public Result() {
    }

    public Result(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    public Result(int code, String msg, Object data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }
    }

    public static Result success() {
        return success("操作成功");
    }

    public static Result success(Object data) {
        return success("操作成功", data);
    }

    public static Result success(String msg, Object data) {
        return new Result(Constants.SUCCESS, msg, data);
    }

    public static Result error() {
        return error("操作失败");
    }

    public static Result error(String msg) {
        return error(Constants.FAIL, msg);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg, null);
    }

    /** 支持链式追加扩展键 */
    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
