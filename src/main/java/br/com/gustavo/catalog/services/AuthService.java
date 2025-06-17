package br.com.gustavo.catalog.services;

import br.com.gustavo.catalog.dto.EmailDTO;
import br.com.gustavo.catalog.entities.PasswordRecover;
import br.com.gustavo.catalog.repositories.PasswordRecoverRepository;
import br.com.gustavo.catalog.repositories.UserRepository;
import br.com.gustavo.catalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    @Value("${email.password-recover.token.minutes}")
    private Long tokenMinutes;

    @Value("${email.password-recover.uri}")
    private String recoverUri;

    private final UserRepository userRepository;
    private final PasswordRecoverRepository passwordRecoverRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordRecoverRepository passwordRecoverRepository, EmailService emailService) {
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
}
