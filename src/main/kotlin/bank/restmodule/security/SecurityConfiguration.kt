package bank.restmodule.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .inMemoryAuthentication()
            .withUser("admin").password(passwordEncoder().encode("admin1234")).roles("ADMIN")
    }

    // only ADMIN can see all users
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers("/users").hasRole("ADMIN")
            .and()
            .httpBasic()
            .and()
            .anonymous()

    }

    // Ignoring all endpoints, not including '/users' one
    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/api/",
            "/api/**")
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
