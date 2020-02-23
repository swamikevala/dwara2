package org.ishafoundation.dwaraapi.authn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment env;
	
	//@Autowired
	//protected NodeumConfiguration nodeumConfig;
	
    @Autowired
    private AuthenticationEntryPoint authEntryPoint;

    @Bean
    CorsFilter corsFilter(){
        CorsFilter filter = new CorsFilter();
        return filter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(corsFilter(), SessionManagementFilter.class) //adds your custom CorsFilter
                .exceptionHandling().authenticationEntryPoint(authEntryPoint)

//                .and()
//                .formLogin()
//                .successHandler(ajaxSuccessHandler)
//                .failureHandler(ajaxFailureHandler)
//                .loginProcessingUrl("/login")
//                .passwordParameter("password")
//                .usernameParameter("username")

                .and()
                .logout()
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")

                .and()
                .csrf().disable()
                .authorizeRequests().antMatchers("/admin/**").permitAll().anyRequest().hasRole("ADMIN")
                .and().authorizeRequests().antMatchers("/ingest/**").permitAll().anyRequest().hasRole("USER")
                .and().authorizeRequests().antMatchers("/contentgroup/**").permitAll().anyRequest().hasRole("USER")
                .and().authorizeRequests().antMatchers("/sequencescheme/**").permitAll().anyRequest().hasRole("USER")
                .anyRequest().authenticated()
                .and().httpBasic()
                .authenticationEntryPoint(authEntryPoint);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().
        withUser(env.getProperty("authn.user1.name")).password("{noop}" + env.getProperty("authn.user1.pwd")).roles("USER").
        and().
        withUser(env.getProperty("authn.user2.name")).password("{noop}" + env.getProperty("authn.user2.pwd")).roles("USER").
        and().
        withUser(env.getProperty("authn.user3.name")).password("{noop}" + env.getProperty("authn.user3.pwd")).roles("USER").
        and().
        withUser(env.getProperty("authn.admin1.name")).password("{noop}" + env.getProperty("authn.admin1.pwd")).roles("USER","ADMIN").
        and().
        withUser(env.getProperty("authn.admin2.name")).password("{noop}" + env.getProperty("authn.admin2.pwd")).roles("USER","ADMIN");
    }

}