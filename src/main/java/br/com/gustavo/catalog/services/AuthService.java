package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.dto.EmailDTO;
import br.com.gustavo.catalog.dto.NewPasswordDTO;
import br.com.gustavo.catalog.entities.PasswordRecover;
import br.com.gustavo.catalog.entities.User;
import br.com.gustavo.catalog.repositories.PasswordRecoverRepository;
import br.com.gustavo.catalog.repositories.UserRepository;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordRecoverRepository passwordRecoverRepository;
    private final EmailService emailService;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, PasswordRecoverRepository passwordRecoverRepository, EmailService emailService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.passwordRecoverRepository = passwordRecoverRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createRecoverToken(EmailDTO dto) {

        // lógica para ver se email existe no banco de dados
        var user = userRepository.findByEmail(dto.getEmail());
        if (user == null) {
            throw new ResourceNotFoundException("Email não encontrado");
        }

        String token = UUID.randomUUID().toString();

        PasswordRecover passwordRecover = new PasswordRecover();
        passwordRecover.setEmail(dto.getEmail());
        passwordRecover.setToken(token);
        passwordRecover.setExpiration(Instant.now().plusSeconds(tokenMinutes * 60L));
        passwordRecover = passwordRecoverRepository.save(passwordRecover);

        String body = "Acesse o link para redefinir a sua senha\n\n" + recoverUri + token + ". Validade de " + tokenMinutes + " minutos";

        emailService.sendEmail(dto.getEmail(), "Recuperação de senha", body);
    }

    // atualizando senha do usuario, se o token for válido (caso o token n tenha expirado)
    @Transactional
    public void saveNewPassword(NewPasswordDTO dto) {

        // aqui tentamos passar uma lista de token válidos (que n tenham expirado), se expirar ele retorna erro (ResourceNotFoundException), se n ele continua a lógica
        List<PasswordRecover> result = passwordRecoverRepository.searchValidTokens(dto.getToken(), Instant.now());
        if (result.size() == 0) {
            throw new ResourceNotFoundException("Token inválido");
        }

        // buscamos o usuario pelo email
        // usuario redefine a nova senha e salvamos no banco de dados com bcrypt
        // salvamos nossa senha do usuario
        var user = userRepository.findByEmail(result.get(0).getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user = userRepository.save(user);
    }

    // obtendo usuario logado
    protected User authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return userRepository.findByEmail(username);
        }
        catch (Exception e) {
            throw new UsernameNotFoundException("Invalid user");
        }
    }
}
