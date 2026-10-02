package com.fatec.todolist.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import com.fatec.todolist.model.repository.TarefaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class TarefaControllerTest {

    @Autowired
    MockMvc requisicoes;

    @Autowired
    ObjectMapper conversorJson;

    @Autowired
    TarefaRepository tarefaRepository;

    @BeforeEach
    void limparBanco() {
        tarefaRepository.deleteAll();
    }

    private long criar(String json) throws Exception {
        MvcResult resultado = requisicoes.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn();
        return conversorJson.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void criaTarefaComStatusPadraoEDatas() throws Exception {
        requisicoes.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Estudar\",\"descricao\":\"Spring\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.dataCriacao").exists())
                .andExpect(jsonPath("$.dataAtualizacao").exists());
    }

    @Test
    void naoCriaSemNome() throws Exception {
        requisicoes.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alteraTarefa() throws Exception {
        long id = criar("{\"nome\":\"A\"}");
        requisicoes.perform(put("/api/tarefas/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"B\",\"status\":\"CONCLUIDA\",\"observacoes\":\"feito\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("B"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoes").value("feito"));
    }

    @Test
    void deletaTarefa() throws Exception {
        long id = criar("{\"nome\":\"A\"}");
        requisicoes.perform(delete("/api/tarefas/" + id)).andExpect(status().isNoContent());
        requisicoes.perform(get("/api/tarefas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void listaTarefas() throws Exception {
        criar("{\"nome\":\"A\"}");
        requisicoes.perform(get("/api/tarefas")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("A"));
    }

    @Test
    void alterarInexistenteRetorna404() throws Exception {
        requisicoes.perform(put("/api/tarefas/9999").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"X\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscaTarefaPorId() throws Exception {
        long id = criar("{\"nome\":\"Estudar Java\"}");
        requisicoes.perform(get("/api/tarefas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Estudar Java"));
    }

    @Test
    void rejeitaStatusInvalido() throws Exception {
        requisicoes.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Estudar\",\"status\":\"INVALIDO\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, tarefaRepository.count());
    }

    @Test
    void rejeitaNomeMuitoGrande() throws Exception {
        String corpo = conversorJson.writeValueAsString(java.util.Map.of("nome", "a".repeat(151)));
        requisicoes.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
        assertEquals(0, tarefaRepository.count());
    }

    @Test
    void naoAlteraTarefaComNomeEmBranco() throws Exception {
        long id = criar("{\"nome\":\"Nome original\"}");
        requisicoes.perform(put("/api/tarefas/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"   \"}"))
                .andExpect(status().isBadRequest());
        assertEquals("Nome original", tarefaRepository.findById(id).orElseThrow().getNome());
    }

    @Test
    void excluirInexistenteRetorna404() throws Exception {
        requisicoes.perform(delete("/api/tarefas/9999")).andExpect(status().isNotFound());
    }

    @Test
    void atualizaDataEPreservaDataDeCriacao() throws Exception {
        long id = criar("{\"nome\":\"Estudar\",\"status\":\"EM_ANDAMENTO\"}");
        var original = tarefaRepository.findById(id).orElseThrow();
        var resultado = requisicoes.perform(put("/api/tarefas/" + id)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Revisar\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andReturn();
        var resposta = conversorJson.readTree(resultado.getResponse().getContentAsString());
        var atualizada = tarefaRepository.findById(id).orElseThrow();
        assertEquals(original.getDataCriacao(), atualizada.getDataCriacao());
        assertTrue(atualizada.getDataAtualizacao().isAfter(original.getDataAtualizacao()));
        // O H2 e o PostgreSQL armazenam timestamps com precisão de microssegundos.
        var dataResposta = LocalDateTime.parse(resposta.get("dataAtualizacao").asText());
        long diferenca = java.time.Duration.between(dataResposta, atualizada.getDataAtualizacao()).toNanos();
        assertTrue(Math.abs(diferenca) <= 1000);
    }
}
