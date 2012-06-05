package fr.ippon.wip.ltpa.exception;

public class Base64DecodeException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -5600202677007235761L;

    /**
     *
     */
    public Base64DecodeException() {
        // Auto-generated constructor stub
    }

    /**
     * @param argMessage
     */
    public Base64DecodeException(String argMessage) {
        super(argMessage);
    }

    /**
     * @param argCause
     */
    public Base64DecodeException(Throwable argCause) {
        super(argCause);
    }

    /**
     * @param argMessage
     * @param argCause
     */
    public Base64DecodeException(String argMessage, Throwable argCause) {
        super(argMessage, argCause);
    }

}
