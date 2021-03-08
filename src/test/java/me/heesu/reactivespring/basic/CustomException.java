package me.heesu.reactivespring.basic;

public class CustomException extends RuntimeException {
    private String msg;

    public CustomException(Throwable e) {
        this.msg = e.getMessage();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
