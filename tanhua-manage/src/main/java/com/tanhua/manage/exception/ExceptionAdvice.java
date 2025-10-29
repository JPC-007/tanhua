package com.tanhua.manage.exception;



import com.tanhua.manage.vo.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 统一异常处理
 */
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleExcepting(Exception e) {
        e.printStackTrace();
        String message = "服务器内部错误";
        if(e instanceof BusinessException) {
            message = ( (BusinessException) e).getMessage();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CommonResponse(message));
    }
}
