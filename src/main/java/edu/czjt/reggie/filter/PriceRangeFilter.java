package edu.czjt.reggie.filter;

import com.alibaba.fastjson.JSON;
import edu.czjt.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebFilter(filterName = "priceRangeFilter", urlPatterns = "/*")  //通过使用@WebFilter注解自动注册过滤器
@Slf4j
public class PriceRangeFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("本次拦截到请求：{}", requestURI);

        // 判断本次请求是否需要验证
        // 不需要验证的url
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout"
        };
        boolean check = check(urls, requestURI);

        // 如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要进行价格范围过滤", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 获取价格范围参数
        String minPriceStr = request.getParameter("minPrice");
        String maxPriceStr = request.getParameter("maxPrice");

        // 判断价格范围是否有效
        if (isValidPriceRange(minPriceStr, maxPriceStr)) {
            BigDecimal minPrice = new BigDecimal(minPriceStr);
            BigDecimal maxPrice = new BigDecimal(maxPriceStr);

            // 判断价格是否在范围内
            BigDecimal itemPrice = new BigDecimal("100.00");  // 假设获取到的商品价格为100.00
            if (itemPrice.compareTo(minPrice) >= 0 && itemPrice.compareTo(maxPrice) <= 0) {
                // 商品价格在范围内，继续处理请求
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 商品价格不在范围内，返回错误结果
        log.info("请求的商品价格不符合过滤器要求");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSON.toJSONString(R.error("请求的商品价格不符合过滤器要求")));
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断价格范围是否有效
     * @param minPriceStr
     * @param maxPriceStr
     * @return
     */
    public boolean isValidPriceRange(String minPriceStr, String maxPriceStr) {
        try {
            BigDecimal minPrice = new BigDecimal(minPriceStr);
            BigDecimal maxPrice = new BigDecimal(maxPriceStr);
            return minPrice.compareTo(BigDecimal.ZERO) >= 0 && maxPrice.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
//该过滤器同样使用了@WebFilter注解标记为一个过滤器，过滤器的名称为priceRangeFilter，并对所有请求进行过滤处理。
//过滤器实现了Filter接口，并实现了doFilter方法用于处理过滤逻辑。
//在doFilter方法中，首先获取当前请求的URI，并记录日志信息。
//判断当前请求是否需要进行验证，定义了一个不需要验证的URL数组，包含了"/employee/login"和"/employee/logout"两个URL。
//如果当前请求不需要验证，则直接放行，调用filterChain.doFilter方法将请求传递给下一个过滤器或目标资源。
//如果当前请求需要验证，则获取请求参数中的最小价格(minPrice)和最大价格(maxPrice)。
//判断价格范围是否有效，调用isValidPriceRange方法进行判断。
//isValidPriceRange方法中，将最小价格和最大价格转换为BigDecimal类型，并判断它们是否大于等于零，以确定价格范围是否有效。
//如果价格范围有效，继续处理请求。
//在假设获取到的商品价格为100.00的情况下，判断商品价格是否在价格范围内。
//如果商品价格在范围内，继续处理请求，调用filterChain.doFilter方法。
//如果商品价格不在范围内，返回一个错误结果。设置响应的字符编码为UTF-8，并通过输出流方式向客户端页面返回一个JSON格式的错误信息。
//过滤器中还定义了一个辅助方法check，用于检查当前请求的URI是否需要放行，该方法通过路径匹配的方式进行判断。
