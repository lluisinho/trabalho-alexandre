package com.fatec.todolist.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fatec.todolist.dto.TarefaDTO;
import com.fatec.todolist.model.entity.Tarefa;
import com.fatec.todolist.service.TarefaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tarefa criar(@Valid @RequestBody TarefaDTO dadosTarefa) {
        return tarefaService.criar(converter(dadosTarefa));
    }

    @GetMapping
    public List<Tarefa> listar() {
        return tarefaService.listar();
    }

    @GetMapping("/{id}")
    public Tarefa buscar(@PathVariable Long id) {
        return tarefaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Tarefa atualizar(@PathVariable Long id, @Valid @RequestBody TarefaDTO dadosTarefa) {
        return tarefaService.atualizar(id, converter(dadosTarefa));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        tarefaService.deletar(id);
    }

    private Tarefa converter(TarefaDTO dadosTarefa) {
        Tarefa tarefa = new Tarefa();
        tarefa.setNome(dadosTarefa.nome());
        tarefa.setDescricao(dadosTarefa.descricao());
        tarefa.setStatus(dadosTarefa.status());
        tarefa.setObservacoes(dadosTarefa.observacoes());
        return tarefa;
    }
}
