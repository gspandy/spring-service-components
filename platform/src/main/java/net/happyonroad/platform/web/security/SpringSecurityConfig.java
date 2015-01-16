/**
 * Developer: Kadvin Date: 14-7-16 下午9:53
 */
package net.happyonroad.platform.web.security;

import net.happyonroad.platform.web.support.DefaultAuthenticationFailureHandler;
import net.happyonroad.platform.web.support.DefaultAuthenticationSuccessHandler;
import net.happyonroad.platform.web.support.DefaultLogoutSuccessHandler;
import net.happyonroad.platform.web.support.LoginPageDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Work as parent of SpringMvcConfig
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter implements DelegateSecurityConfigurer {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().requireCsrfProtectionMatcher(new CustomMatcher());
        http.exceptionHandling().accessDeniedHandler(new LoginPageDeniedHandler());
        http.authenticationProvider(delegateAuthenticationProvider())
                // 配置了 authentication provider 之后， 不需要配置 user details service
                //.userDetailsService(delegateUserDetailsService())
                /* 配置 remember me*/
                .rememberMe()
                .authenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler())
                .tokenRepository(delegateTokenRepository())
                .tokenValiditySeconds(60 * 60 * 24 * 30)//一个月
                .useSecureCookie(true)
                /* 配置匿名服务 */
                .and().anonymous().authorities("ROLE_ANONYMOUS").principal("ANONYMOUS")
                /* 配置登出服务 */
                .and().logout().invalidateHttpSession(true).logoutSuccessUrl("/login.html")
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/session", "DELETE"))
                .logoutSuccessHandler(new DefaultLogoutSuccessHandler())
                /* 配置会话服务 */
                .and().sessionManagement().enableSessionUrlRewriting(true).sessionFixation().migrateSession();
        // 若以后支持手机客户端访问，那个时候可能就需要基于Digest-Authentication
        //noinspection unchecked
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authenticated =
                (ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry) http.authorizeRequests();
        //谁在前，谁优先级高
        authenticated.antMatchers(HttpMethod.POST, "api/session").anonymous()
                     .antMatchers("/login.html").anonymous() // use spring interceptor
                .antMatchers("/public/api/**").anonymous()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/admin/api/**").hasAnyRole("ADMIN")
                .antMatchers("/index.html").not().anonymous()
                .antMatchers("/routes").hasRole("ANONYMOUS")
                .antMatchers("/**").permitAll();
        authenticated.and().formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/api/session")
                .successHandler(new DefaultAuthenticationSuccessHandler())
                .failureHandler(new DefaultAuthenticationFailureHandler())
                .and().httpBasic().realmName("ItsNow Platform");
    }

    @Bean
    DelegateAuthenticationProvider delegateAuthenticationProvider() {
        return new DelegateAuthenticationProvider();
    }

    @Bean
    DelegatePersistentTokenRepository delegateTokenRepository() {
        return new DelegatePersistentTokenRepository();
    }

    // Delegate Security Configurer

    @Override
    public DelegateSecurityConfigurer delegate(AuthenticationProvider delegate) {
        delegateAuthenticationProvider().setDelegate(delegate);
        return this;
    }

    @Override
    public DelegateSecurityConfigurer delegate(PersistentTokenRepository delegate) {
        delegateTokenRepository().setDelegate(delegate);
        return this;
    }

    private static final class CustomMatcher implements RequestMatcher {
        private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

        /* (non-Javadoc)
         * @see org.springframework.security.web.util.matcher.RequestMatcher#matches(javax.servlet.http.HttpServletRequest)
         */
        public boolean matches(HttpServletRequest request) {
            if ( request.getPathInfo().startsWith("/south") || request.getPathInfo().startsWith("/north") )
               return false;
            return !allowedMethods.matcher(request.getMethod()).matches();
        }
    }
}
