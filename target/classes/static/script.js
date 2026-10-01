// Os campos do formulário e os elementos que vamos atualizar.
const formulario = document.getElementById('formulario');
const nome = document.getElementById('nome');
const descricao = document.getElementById('descricao');
const status = document.getElementById('status');
const observacoes = document.getElementById('observacoes');
const lista = document.getElementById('lista');
const mensagem = document.getElementById('mensagem');
const salvar = document.getElementById('salvar');
const cancelar = document.getElementById('cancelar');
const situacaoLista = document.getElementById('situacao-lista');

// Sem ID, o formulário cria uma tarefa. Com ID, ele edita a tarefa.
let idEmEdicao = null;

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.className = erro ? 'erro' : '';
}

// fetch envia uma requisição para o controller Java.
// await espera a resposta antes de continuar a função.
async function verificarResposta(resposta) {
    if (!resposta.ok) {
        let texto = 'Não foi possível realizar a operação.';
        try {
            const dados = await resposta.json();
            if (dados.erro) texto = dados.erro;
        } catch (erro) {
            texto = 'O servidor não retornou uma resposta válida.';
        }
        throw new Error(texto);
    }
}

async function listarTarefas() {
    situacaoLista.textContent = 'Carregando tarefas...';
    try {
        const resposta = await fetch('/api/tarefas');
        await verificarResposta(resposta);
        const tarefas = await resposta.json();
        lista.replaceChildren();
        for (const tarefa of tarefas) {
            mostrarTarefa(tarefa);
        }
        situacaoLista.textContent = tarefas.length === 0
            ? 'Nenhuma tarefa cadastrada. Crie a primeira no formulário.'
            : tarefas.length + ' tarefa(s) cadastrada(s).';
    } catch (erro) {
        situacaoLista.textContent = 'Não foi possível atualizar a lista. Clique em Atualizar para tentar novamente.';
        mostrarMensagem('Erro ao carregar tarefas: ' + erro.message, true);
    }
}

function mostrarTarefa(tarefa) {
    const cartao = document.createElement('article');
    cartao.className = 'tarefa';

    const titulo = document.createElement('h3');
    titulo.textContent = tarefa.nome;
    cartao.appendChild(titulo);

    const etiqueta = document.createElement('span');
    etiqueta.className = 'status ' + tarefa.status;
    const nomesStatus = { PENDENTE: 'Pendente', EM_ANDAMENTO: 'Em andamento', CONCLUIDA: 'Concluída' };
    etiqueta.textContent = nomesStatus[tarefa.status];
    cartao.appendChild(etiqueta);

    // textContent mostra o texto recebido sem interpretá-lo como HTML.
    if (tarefa.descricao) {
        const texto = document.createElement('p');
        texto.textContent = tarefa.descricao;
        cartao.appendChild(texto);
    }
    if (tarefa.observacoes) {
        const texto = document.createElement('p');
        texto.textContent = 'Observações: ' + tarefa.observacoes;
        cartao.appendChild(texto);
    }

    const acoes = document.createElement('div');
    acoes.className = 'acoes';
    const editar = document.createElement('button');
    editar.textContent = 'Editar';
    editar.className = 'secundario';
    editar.onclick = function () { editarTarefa(tarefa); };
    const excluir = document.createElement('button');
    excluir.textContent = 'Excluir';
    excluir.className = 'excluir';
    excluir.onclick = function () { excluirTarefa(tarefa, excluir); };
    acoes.appendChild(editar);
    acoes.appendChild(excluir);
    cartao.appendChild(acoes);
    lista.appendChild(cartao);
}

function editarTarefa(tarefa) {
    idEmEdicao = tarefa.id;
    nome.value = tarefa.nome;
    descricao.value = tarefa.descricao || '';
    status.value = tarefa.status;
    observacoes.value = tarefa.observacoes || '';
    document.getElementById('titulo-formulario').textContent = 'Editar tarefa';
    cancelar.hidden = false;
    nome.focus();
}

function limparFormulario() {
    formulario.reset();
    idEmEdicao = null;
    document.getElementById('titulo-formulario').textContent = 'Nova tarefa';
    cancelar.hidden = true;
}

async function salvarTarefa(evento) {
    evento.preventDefault(); // Evita que o formulário recarregue a página.
    if (!nome.value.trim()) {
        mostrarMensagem('Preencha o nome da tarefa.', true);
        nome.focus();
        return;
    }
    const tarefa = {
        nome: nome.value.trim(),
        descricao: descricao.value,
        status: status.value,
        observacoes: observacoes.value
    };
    let url = '/api/tarefas';
    let metodo = 'POST';
    if (idEmEdicao !== null) {
        url = url + '/' + idEmEdicao;
        metodo = 'PUT';
    }
    salvar.disabled = true;
    try {
        const resposta = await fetch(url, {
            method: metodo,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(tarefa)
        });
        await verificarResposta(resposta);
        limparFormulario();
        mostrarMensagem('Tarefa salva com sucesso!');
        await listarTarefas();
    } catch (erro) {
        mostrarMensagem('Erro ao salvar: ' + erro.message, true);
    } finally {
        salvar.disabled = false;
    }
}

async function excluirTarefa(tarefa, botao) {
    if (!confirm('Excluir a tarefa "' + tarefa.nome + '"?')) return;
    botao.disabled = true;
    try {
        const resposta = await fetch('/api/tarefas/' + tarefa.id, { method: 'DELETE' });
        await verificarResposta(resposta);
        if (idEmEdicao === tarefa.id) limparFormulario();
        mostrarMensagem('Tarefa excluída.');
        await listarTarefas();
    } catch (erro) {
        mostrarMensagem('Erro ao excluir: ' + erro.message, true);
    } finally {
        botao.disabled = false;
    }
}

formulario.onsubmit = salvarTarefa;
cancelar.onclick = limparFormulario;
document.getElementById('atualizar').onclick = function () {
    mostrarMensagem('');
    listarTarefas();
};
listarTarefas();
