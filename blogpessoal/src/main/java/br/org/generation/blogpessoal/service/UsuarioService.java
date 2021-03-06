package br.org.generation.blogpessoal.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.org.generation.blogpessoal.model.Usuario;
import br.org.generation.blogpessoal.model.UsuarioLogin;
import br.org.generation.blogpessoal.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
		if(usuarioRepository.findByLogin(usuario.getLogin()).isPresent())
			return null;
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		String senhaEncoder = encoder.encode(usuario.getSenha());
		usuario.setSenha(senhaEncoder);
		
		return Optional.of(usuarioRepository.save(usuario));
	}
	
	public Optional<Usuario> atualizarUsuario(Usuario usuario){
		if(usuarioRepository.findById(usuario.getId()).isPresent()) {
			
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			
			String senhaEncoder = encoder.encode(usuario.getSenha());
			usuario.setSenha(senhaEncoder);
			
			return Optional.of(usuarioRepository.save(usuario));
		} else {
			throw new ResponseStatusException (
					HttpStatus.NOT_FOUND, "Usuário não encontrado!", null);
		}
	}
	
	public Optional<UsuarioLogin> logarUsuario(Optional<UsuarioLogin> usuarioLogin){
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<Usuario> usuario = usuarioRepository.findByLogin(usuarioLogin.get().getUsuario());
		
		if (usuario.isPresent()) {
			if (encoder.matches(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {
				
				
				String auth = usuarioLogin.get().getUsuario() + ":" + usuarioLogin.get().getSenha();
				byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodeAuth);
				
				usuarioLogin.get().setToken(authHeader);
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setSenha(usuario.get().getSenha());
				
				return usuarioLogin;
				
			}
		}
		
		return null;
	}

}
