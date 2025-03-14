package app.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggingAspect {

    // Advice для логирования перед выполнением метода
    @Before("@within(app.aspect.annotation.CustomLogging) || @annotation(app.aspect.annotation.CustomLogging)")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Вызов метода: {}", joinPoint.getSignature().getName());
    }

    // Advice для логирования после успешного выполнения метода
    @AfterReturning(pointcut = "@within(app.aspect.annotation.CustomLogging) || @annotation(app.aspect.annotation.CustomLogging)", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Метод {} завершился успешно с результатом: {}", joinPoint.getSignature().getName(), result);
    }

    // Advice для логирования в случае исключения
    @AfterThrowing(pointcut = "@within(app.aspect.annotation.CustomLogging) || @annotation(app.aspect.annotation.CustomLogging)", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        log.error("Метод {} завершился с ошибкой: {}", joinPoint.getSignature().getName(), error.getMessage());
    }

    // Advice для замера времени выполнения метода
    @Around("@within(app.aspect.annotation.CustomLogging) || @annotation(app.aspect.annotation.CustomLogging)")
    public Object logAround(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed(); // Вызов метода
            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Метод {} выполнен за {} миллисекунд", joinPoint.getSignature().getName(), totalTime);
        } catch (Throwable throwable) {
            log.error("Ошибка при выполнении метода {}: {}", joinPoint.getSignature().getName(), throwable.getMessage());
        }
        return result;
    }
}



