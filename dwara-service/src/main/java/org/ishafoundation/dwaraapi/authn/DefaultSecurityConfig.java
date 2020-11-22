package org.ishafoundation.dwaraapi.authn;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class DefaultSecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Autowired
    private AuthenticationEntryPoint authEntryPoint;
    
    @Autowired
    private DataSource dataSource;

    // Angular + SB -
    // https://medium.com/@rameez.s.shaikh/angular-7-spring-boot-basic-authentication-example-98455b73d033
    // http://www.masterspringboot.com/security/authentication/configuring-spring-boot-authentication-using-in-memory-and-database-providers
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	// TODO Brief swami on default schema from spring security
    	// a) its user table structure and the enabled field/column in the table
    	// b) and authorities table...

    	
    	// JdbcUserDetailsManagerConfigurer accepts only 3 columns defined in the default spring security schema like 
    	// select username,password,enabled from users where username = ?
    	// using the app specific custom schema here 
    	auth.jdbcAuthentication().dataSource(dataSource)
    	.usersByUsernameQuery("select name, hash, true " + " from user where name=?")
    	.authoritiesByUsernameQuery("select name, 'ROLE_ADMIN' from user where name=?")
    	.passwordEncoder(new BCryptPasswordEncoder());
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
               .cors().and()
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
                .authorizeRequests().antMatchers("/login").permitAll()
                .and().authorizeRequests().antMatchers("/clearAndReload").permitAll()
                //.and().authorizeRequests().antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**").permitAll()
                .and().authorizeRequests().antMatchers("/register").permitAll()
                .and().authorizeRequests().antMatchers("/setpassword").permitAll()
                .anyRequest().authenticated()
                .and().httpBasic()
                .authenticationEntryPoint(authEntryPoint);
    }

    @Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager() throws Exception {
    	JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
    	jdbcUserDetailsManager.setDataSource(dataSource);
    	return jdbcUserDetailsManager;
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource()
    {
	    CorsConfiguration configuration = new CorsConfiguration();
	    configuration.applyPermitDefaultValues();
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", configuration);
	    return source;
    }
}