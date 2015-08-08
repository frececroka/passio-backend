package passio;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CorsInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object o) throws Exception {
		res.setHeader("Access-Control-Allow-Origin", "*");
		res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
		res.setHeader("Access-Control-Allow-Headers", "X-Signing-Key, X-MAC");
		return !req.getMethod().equals("OPTIONS");
	}

	@Override
	public void postHandle(HttpServletRequest req, HttpServletResponse res, Object o, ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object o, Exception e) throws Exception {
	}

}
