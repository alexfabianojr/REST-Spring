package br.com.alura.forum.config.security;

import br.com.alura.forum.modelo.Usuario;
import br.com.alura.forum.repository.UsuarioRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AutenticacaoViaTokenFilter extends OncePerRequestFilter {

//    O filtro foi instanciado manualmente por nós, na classe SecurityConfigurations
//    e portanto o Spring não consegue realizar injeção de dependências via @Autowired.
    private TokenService tokenService;

    private UsuarioRepository usuarioRepository;

    public AutenticacaoViaTokenFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = recuperarToken(request);
        boolean valido = tokenService.isTokenValido(token);
        System.out.println(valido);
        if (valido) {
            autenticarCliente(token);
        }

        filterChain.doFilter(request, response);


    }

    private void autenticarCliente(String token) {
        Long idUsuario = tokenService.getIdUsuario(token);
        Usuario usuario = usuarioRepository.findById(idUsuario).get();
        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

//Por que essa autenticação foi feita com a classe SecurityContextHolder e não com a AuthenticationManager?
//A classe AuthenticationManager deve ser utilizada apenas na lógica de autenticação via username/password, para a geração do token.

    private String recuperarToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization"); //o head eh definido la no postman (cliente)
        if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) return null;
        return token.substring(7, token.length());
    }
}
