package licenta.medicaldata.config;

import licenta.medicaldata.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource(name = "userDetailService")
    private UserDetailsServiceImpl userDetailsService;

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/css/**", "/index").permitAll()
                .antMatchers( "/admin/**").hasRole("admin")
                .antMatchers("/pacient/**").hasRole("pacient")
                .antMatchers("/doctor/**").hasRole("doctor")
                .antMatchers("/lab/**").hasRole("lab")
                .antMatchers("/analize/**").hasAnyRole("pacient", "doctor")
                .and()
                .formLogin()
                .loginPage("/login").failureUrl("/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                        Authentication authentication) throws IOException, ServletException {
                        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                        authorities.forEach(authority -> {
                            if(authority.getAuthority().equals("ROLE_admin")) {
                                try {
                                    redirectStrategy.sendRedirect(request, response, "/admin/addUser");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if(authority.getAuthority().equals("ROLE_lab")) {
                                try {
                                    redirectStrategy.sendRedirect(request, response, "/lab/upload");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if(authority.getAuthority().equals("ROLE_pacient")) {
                                try {
                                    User user =(User) SecurityContextHolder.getContext()
                                            .getAuthentication()
                                            .getPrincipal();
                                    Integer id = userDetailsService.getByEmail(user.getUsername()).getId();
                                    redirectStrategy.sendRedirect(request, response, "/analize/"+id);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if(authority.getAuthority().equals("ROLE_doctor")) {
                                try {

                                    redirectStrategy.sendRedirect(request, response, "/doctor/pacienti");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                throw new IllegalStateException();
                            }
                        });
                    }
                });
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }




}
