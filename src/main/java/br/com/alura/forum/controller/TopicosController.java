package br.com.alura.forum.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import br.com.alura.forum.controller.dto.DetalhesDoTopicoDto;
import br.com.alura.forum.controller.form.AtualizacaoTopicoForm;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import br.com.alura.forum.controller.dto.TopicoDto;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.TopicoRepository;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;

	@Autowired
	private CursoRepository cursoRepository;

	//Dto(s) são imporantes para não ficar retornando a entidade da JPA (por segurança)
	@GetMapping
	public List<TopicoDto> lista(String nomeCurso) {
		List<Topico> topicos;
		if (nomeCurso == null) {
			topicos = topicoRepository.findAll();
		} else {
			topicos = topicoRepository.findByCursoNome(nomeCurso);
		}
		return TopicoDto.converter(topicos);
	}

	@PostMapping
	public ResponseEntity<TopicoDto> cadastrar(@RequestBody @Valid TopicoForm form, UriComponentsBuilder builder) {
		Topico topico = form.converter(cursoRepository);
		topicoRepository.save(topico);
		URI uri = builder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
		return ResponseEntity.created(uri).body(new TopicoDto(topico));
	}

	@GetMapping("/{id}")
	public ResponseEntity<DetalhesDoTopicoDto> detalhar(@PathVariable("id") Long id) {
		Optional<Topico> topico = topicoRepository.findById(id);
		//	if (topico.isPresent()) return ResponseEntity.ok(new DetalhesDoTopicoDto(topico.get()));
//		else return ResponseEntity.notFound().build();    metodo antigo trocado pela abstracao
		return topico.map(value -> ResponseEntity.ok(new DetalhesDoTopicoDto(value)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	@Transactional //Ao finalizar o método, o Spring efetuará o commit automático da transação, caso nenhuma exception tenha sido lançada
	public ResponseEntity<TopicoDto> atualizar(@PathVariable("id") Long id,@RequestBody @Valid AtualizacaoTopicoForm atualizaForm) {
		Optional<Topico> topico = topicoRepository.findById(id);
		return topico.map(value -> ResponseEntity.ok(new TopicoDto(topico.get())))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<?> remover(@PathVariable Long id) {
		Optional<Topico> topico = topicoRepository.findById(id);
		if (topico.isPresent()) {
			topicoRepository.deleteById(id);
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
