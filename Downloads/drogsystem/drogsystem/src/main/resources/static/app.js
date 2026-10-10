const API_ROOT = "/api";
const storageKey = "drogsystem.auth";

const PROFILE_LABELS = {
    ADMINISTRADOR: "Administrador",
    FARMACEUTICO: "Farmaceutico",
    ATENDENTE: "Atendente",
    CLIENTE: "Cliente"
};

const CATEGORY_OPTIONS = ["CONTROLADO", "GENERICO", "COSMETICO", "SUPLEMENTO", "HIGIENE_PESSOAL", "OUTRO"];
const PAYMENT_OPTIONS = ["DINHEIRO", "CARTAO_CREDITO", "CARTAO_DEBITO", "PIX"];
const DISCOUNT_OPTIONS = ["PERCENTUAL", "VALOR_FIXO"];
const PROFILE_OPTIONS = ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE", "CLIENTE"];

const modules = [
    { id: "dashboard", label: "Visao geral", hint: "Indicadores e resumos", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE", "CLIENTE"] },
    { id: "produtos", label: "Produtos", hint: "Catalogo e estoque minimo", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE"] },
    { id: "estoque", label: "Estoque", hint: "Entradas, saidas e lotes", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE"] },
    { id: "vendas", label: "Vendas", hint: "Checkout e cupons", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE"] },
    { id: "promocoes", label: "Promocoes", hint: "Campanhas ativas", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE"] },
    { id: "reservas", label: "Reservas", hint: "Separacao e retirada", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE", "CLIENTE"] },
    { id: "clientes", label: "Clientes", hint: "Perfil e historico", roles: ["ADMINISTRADOR", "FARMACEUTICO", "CLIENTE"] },
    { id: "usuarios", label: "Usuarios", hint: "Time e acessos", roles: ["ADMINISTRADOR", "FARMACEUTICO"] },
    { id: "notificacoes", label: "Notificacoes", hint: "Alertas e avisos", roles: ["ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE", "CLIENTE"] },
    { id: "relatorios", label: "Relatorios", hint: "Indicadores e exportacoes", roles: ["ADMINISTRADOR", "FARMACEUTICO"] }
];

const state = {
    auth: loadAuth(),
    view: "dashboard",
    busy: false,
    status: null,
    products: [],
    productMap: new Map(),
    customers: [],
    promotions: [],
    users: [],
    saleCoupon: "",
    modalTitle: "",
    saleItems: [{ produtoId: "", quantidade: 1 }]
};

const app = document.getElementById("app");

document.addEventListener("DOMContentLoaded", init);

function init() {
    render();
}

function loadAuth() {
    try {
        const raw = sessionStorage.getItem(storageKey);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function persistAuth(auth) {
    state.auth = auth;
    if (auth) {
        sessionStorage.setItem(storageKey, JSON.stringify(auth));
    } else {
        sessionStorage.removeItem(storageKey);
    }
}

function setStatus(kind, message) {
    state.status = { kind, message };
    renderStatus();
}

function renderStatus() {
    const node = document.getElementById("status-bar");
    if (!node) {
        return;
    }
    if (!state.status) {
        node.innerHTML = "";
        return;
    }
    node.innerHTML = `<div class="status-bar status-${state.status.kind}">${escapeHtml(state.status.message)}</div>`;
}

function clearStatus() {
    state.status = null;
    renderStatus();
}

function setBusy(flag) {
    state.busy = flag;
    document.querySelectorAll("button").forEach((button) => {
        if (flag) {
            button.setAttribute("disabled", "disabled");
        } else if (!button.dataset.keepDisabled) {
            button.removeAttribute("disabled");
        }
    });
}

function hasRole(...roles) {
    return Boolean(state.auth && roles.includes(state.auth.perfil));
}

function availableModules() {
    if (!state.auth) {
        return [];
    }
    return modules.filter((module) => module.roles.includes(state.auth.perfil));
}

function render() {
    app.innerHTML = state.auth ? renderShell() : renderLogin();
    bindGlobalEvents();
    if (state.auth) {
        renderStatus();
        renderView();
    }
}

function renderLogin() {
    return `
        <main class="screen">
            <div class="login-shell">
                <section class="hero">
                    <div class="stack">
                        <span class="eyebrow">Drogsystem</span>
                        <h1>Farmacia, estoque e vendas no mesmo painel.</h1>
                        <p>Esta interface fecha o ciclo do projeto consumindo a API Spring existente: login, operacao diaria, relatorios e acompanhamento por perfil.</p>
                    </div>
                    <div class="hero-grid">
                        <div class="hero-stat">
                            <span class="muted">Operacao</span>
                            <strong>9 modulos</strong>
                        </div>
                        <div class="hero-stat">
                            <span class="muted">Perfis</span>
                            <strong>4 acessos</strong>
                        </div>
                        <div class="hero-stat">
                            <span class="muted">Relatorios</span>
                            <strong>PDF + XLSX</strong>
                        </div>
                    </div>
                </section>
                <section class="panel">
                    <div class="stack">
                        <span class="pill">Acesso</span>
                        <h2>Entrar no sistema</h2>
                        <p>Use o usuario inicial criado pelo DataInitializer para o primeiro acesso.</p>
                        <form id="login-form" class="stack">
                            <div class="field">
                                <label for="login-email">E-mail</label>
                                <input id="login-email" name="email" type="email" autocomplete="username" placeholder="admin@drogsystem.com" required>
                            </div>
                            <div class="field">
                                <label for="login-senha">Senha</label>
                                <input id="login-senha" name="senha" type="password" autocomplete="current-password" placeholder="admin123" required>
                            </div>
                            <button class="button" type="submit">Entrar</button>
                        </form>
                        <div id="status-bar"></div>
                        <div class="card">
                            <strong>Primeiro acesso</strong>
                            <p class="muted">Email: admin@drogsystem.com<br>Senha: admin123</p>
                        </div>
                    </div>
                </section>
            </div>
        </main>
    `;
}

function renderShell() {
    const currentModules = availableModules();
    if (!currentModules.some((module) => module.id === state.view)) {
        state.view = currentModules[0]?.id || "dashboard";
    }
    return `
        <main class="screen">
            <div class="app-shell">
                <aside class="shell-sidebar">
                    <div class="brand">
                        <span class="pill">Operacao ativa</span>
                        <strong>Drogsystem</strong>
                        <span class="muted">Controle de farmacia com operacao por perfil.</span>
                    </div>
                    <div class="card">
                        <div class="stack">
                            <span class="muted">Sessao</span>
                            <strong>${escapeHtml(state.auth.email)}</strong>
                            <span class="tag ok">${escapeHtml(PROFILE_LABELS[state.auth.perfil] || state.auth.perfil)}</span>
                        </div>
                    </div>
                    <nav class="nav-list">
                        ${currentModules.map((module) => `
                            <button type="button" class="nav-button ${state.view === module.id ? "active" : ""}" data-nav="${module.id}">
                                <strong>${escapeHtml(module.label)}</strong>
                                <small>${escapeHtml(module.hint)}</small>
                            </button>
                        `).join("")}
                    </nav>
                    <div class="shell-actions">
                        <button type="button" class="ghost-button" data-action="refresh-view">Atualizar modulo</button>
                        <button type="button" class="danger-button" data-action="logout">Sair</button>
                    </div>
                </aside>
                <section class="shell-main">
                    <div class="shell-header">
                        <div>
                            <span class="eyebrow">${escapeHtml(getModuleLabel(state.view))}</span>
                            <h1>${escapeHtml(getModuleTitle(state.view))}</h1>
                            <p class="muted">${escapeHtml(getModuleDescription(state.view))}</p>
                        </div>
                        <div class="row">
                            <button type="button" class="ghost-button" data-action="go-dashboard">Visao geral</button>
                        </div>
                    </div>
                    <div id="status-bar"></div>
                    <div id="view-root"><div class="loader"></div></div>
                </section>
            </div>
        </main>
        <div id="modal" class="modal" aria-hidden="true">
            <div class="modal-box">
                <div class="row space">
                    <h2 id="modal-title">Detalhe</h2>
                    <button type="button" class="ghost-button" data-action="close-modal">Fechar</button>
                </div>
                <div id="modal-content"></div>
            </div>
        </div>
    `;
}

function getModuleLabel(view) {
    return modules.find((module) => module.id === view)?.label || "Modulo";
}

function getModuleTitle(view) {
    const titles = {
        dashboard: "Painel operacional",
        produtos: "Catalogo e disponibilidade",
        estoque: "Movimentacao de estoque",
        vendas: "Checkout e historico",
        promocoes: "Campanhas e descontos",
        reservas: "Reservas e retiradas",
        clientes: "Clientes e fidelidade",
        usuarios: "Equipe e acessos",
        notificacoes: "Notificacoes do usuario",
        relatorios: "Relatorios gerenciais"
    };
    return titles[view] || "Modulo";
}

function getModuleDescription(view) {
    const descriptions = {
        dashboard: "Resumo do que precisa de acao agora, sem trocar de tela.",
        produtos: "Cadastre, edite e acompanhe o catalogo com alertas de estoque minimo.",
        estoque: "Registre entradas e saidas, consulte lotes e acompanhe vencimentos.",
        vendas: "Monte vendas com itens dinamicos, cliente opcional, promocao e cupom.",
        promocoes: "Gerencie campanhas por percentual ou valor fixo e acompanhe vigencia.",
        reservas: "Crie reservas, confirme retiradas e acompanhe situacoes pendentes.",
        clientes: "Veja perfis, pontos, historico e atualize o autoatendimento do cliente.",
        usuarios: "Gerencie o quadro de usuarios com perfil e status de acesso.",
        notificacoes: "Acompanhe e marque alertas relacionados a reservas e operacao.",
        relatorios: "Visualize indicadores em JSON e exporte PDF ou Excel direto da API."
    };
    return descriptions[view] || "";
}

function bindGlobalEvents() {
    const loginForm = document.getElementById("login-form");
    if (loginForm) {
        loginForm.addEventListener("submit", handleLogin);
    }

    document.querySelectorAll("[data-nav]").forEach((button) => {
        button.addEventListener("click", () => {
            clearStatus();
            state.view = button.dataset.nav;
            render();
        });
    });

    document.querySelectorAll("[data-action]").forEach((button) => {
        button.addEventListener("click", handleAction);
    });
}

async function handleLogin(event) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const email = String(form.get("email") || "").trim();
    const senha = String(form.get("senha") || "");
    clearStatus();
    setBusy(true);
    try {
        const result = await fetchJson(`${API_ROOT}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        }, false);
        const basic = btoa(`${email}:${senha}`);
        persistAuth({ email: result.email, perfil: normalizePerfil(result.perfil), basic });
        state.view = availableModules()[0]?.id || "dashboard";
        setStatus("info", `Sessao iniciada para ${result.email}.`);
        render();
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    } finally {
        setBusy(false);
    }
}

function normalizePerfil(perfil) {
    return String(perfil || "").replace(/^ROLE_/, "");
}

async function handleAction(event) {
    const action = event.currentTarget.dataset.action;
    clearStatus();

    if (action === "logout") {
        persistAuth(null);
        state.products = [];
        state.productMap = new Map();
        state.customers = [];
        state.promotions = [];
        state.users = [];
        state.saleItems = [{ produtoId: "", quantidade: 1 }];
        state.saleCoupon = "";
        render();
        return;
    }
    if (action === "refresh-view") {
        await renderView();
        return;
    }
    if (action === "go-dashboard") {
        state.view = "dashboard";
        render();
        return;
    }
    if (action === "close-modal") {
        closeModal();
        return;
    }
    if (action === "add-sale-item") {
        addSaleItemRow();
        return;
    }
    if (action === "mark-all-notifications") {
        await markAllNotifications();
        return;
    }
    if (action === "load-my-history") {
        await loadMyHistory();
        return;
    }
    if (action === "load-my-profile") {
        await loadMyProfile();
        return;
    }
}

async function renderView() {
    const root = document.getElementById("view-root");
    if (!root) {
        return;
    }
    root.innerHTML = '<div class="loader"></div>';
    try {
        if (state.view === "dashboard") {
            await renderDashboard(root);
        } else if (state.view === "produtos") {
            await renderProducts(root);
        } else if (state.view === "estoque") {
            await renderStock(root);
        } else if (state.view === "vendas") {
            await renderSales(root);
        } else if (state.view === "promocoes") {
            await renderPromotions(root);
        } else if (state.view === "reservas") {
            await renderReservations(root);
        } else if (state.view === "clientes") {
            await renderCustomers(root);
        } else if (state.view === "usuarios") {
            await renderUsers(root);
        } else if (state.view === "notificacoes") {
            await renderNotifications(root);
        } else if (state.view === "relatorios") {
            await renderReports(root);
        }
    } catch (error) {
        root.innerHTML = `<div class="empty-state"><div><h3>Falha ao carregar modulo</h3><p>${escapeHtml(getErrorMessage(error))}</p></div></div>`;
    }
}

async function renderDashboard(root) {
    const requests = [];
    if (hasRole("ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE")) {
        requests.push(fetchJson(`${API_ROOT}/produtos`).then((items) => ({ key: "produtos", value: items })));
    }
    if (hasRole("ADMINISTRADOR", "FARMACEUTICO")) {
        requests.push(fetchJson(`${API_ROOT}/produtos/estoque-baixo`).then((items) => ({ key: "estoqueBaixo", value: items })));
        requests.push(fetchJson(`${API_ROOT}/promocoes/vigentes`).then((items) => ({ key: "promocoes", value: items })));
    }
    if (hasRole("ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE", "CLIENTE")) {
        requests.push(fetchJson(`${API_ROOT}/reservas${hasRole("CLIENTE") ? "/minhas" : ""}`).then((items) => ({ key: "reservas", value: items })));
        requests.push(fetchJson(`${API_ROOT}/notificacoes`).then((items) => ({ key: "notificacoes", value: items })));
    }
    if (hasRole("ADMINISTRADOR", "FARMACEUTICO")) {
        requests.push(fetchJson(`${API_ROOT}/relatorios/estoque`).then((items) => ({ key: "relatorioEstoque", value: items })));
    }

    const resolved = await Promise.allSettled(requests);
    const data = {};
    resolved.forEach((result) => {
        if (result.status === "fulfilled") {
            data[result.value.key] = result.value.value;
        }
    });
    const productList = data.produtos || [];
    const notifications = data.notificacoes || [];
    const reservations = data.reservas || [];
    const stockReport = data.relatorioEstoque || null;
    const lowStock = data.estoqueBaixo || [];
    const activePromotions = data.promocoes || [];

    root.innerHTML = `
        <section class="section-hero stack">
            <div class="row space">
                <div>
                    <span class="pill">Resumo em tempo real</span>
                    <h2>O que exige atencao agora</h2>
                    <p>Visao sintetica da operacao do perfil ${escapeHtml(PROFILE_LABELS[state.auth.perfil] || state.auth.perfil)}.</p>
                </div>
                <div class="badge-row">
                    <span class="tag ok">Produtos: ${productList.length}</span>
                    <span class="tag warn">Reservas: ${reservations.length}</span>
                    <span class="tag">Notificacoes: ${notifications.length}</span>
                </div>
            </div>
            <div class="dashboard-grid">
                ${metricCard("Catalogo ativo", productList.length, lowStock.length ? `${lowStock.length} abaixo do minimo` : "Sem alertas criticos")}
                ${metricCard("Promocoes vigentes", activePromotions.length, activePromotions.length ? "Campanhas em vigor" : "Nenhuma campanha ativa")}
                ${metricCard("Reservas abertas", reservations.filter((item) => !["CONFIRMADA", "CANCELADA", "EXPIRADA"].includes(item.status)).length, "Acompanhamento de separacao")}
                ${metricCard("Notificacoes nao lidas", notifications.filter((item) => !item.lida).length, "Pendencias do usuario")}
            </div>
        </section>
        <div class="module-grid">
            <section class="table-card">
                <div class="row space">
                    <h3>Produtos em alerta</h3>
                    <span class="table-caption">Estoque minimo e validade</span>
                </div>
                ${renderSimpleTable(lowStock.slice(0, 8), ["nome", "categoria", "quantidadeEstoque", "estoqueMinimo"], {
                    nome: "Produto",
                    categoria: "Categoria",
                    quantidadeEstoque: "Atual",
                    estoqueMinimo: "Minimo"
                })}
            </section>
            <section class="table-card">
                <div class="row space">
                    <h3>Reservas recentes</h3>
                    <span class="table-caption">Ultimas movimentacoes</span>
                </div>
                ${renderSimpleTable(reservations.slice(0, 8), ["nomeProduto", "clienteNome", "quantidade", "status"], {
                    nomeProduto: "Produto",
                    clienteNome: "Cliente",
                    quantidade: "Qtd",
                    status: "Status"
                }, (key, value) => key === "status" ? badge(value) : escapeHtml(formatValue(value)))}
            </section>
        </div>
        ${stockReport ? `
            <div class="stats-grid">
                <section class="card metric"><span class="muted">Ativos</span><strong>${stockReport.totalProdutosAtivos ?? 0}</strong><span class="delta">Produtos ativos no estoque</span></section>
                <section class="card metric"><span class="muted">Lotes vencidos</span><strong>${stockReport.totalLotesVencidos ?? 0}</strong><span class="delta">Prioridade de acao imediata</span></section>
                <section class="card metric"><span class="muted">A vencer em 30 dias</span><strong>${stockReport.totalLotesAVencer30Dias ?? 0}</strong><span class="delta">Planejamento preventivo</span></section>
            </div>
        ` : ""}
    `;
}

async function renderProducts(root) {
    const [products, lowStock] = await Promise.all([
        fetchProducts(),
        hasRole("ADMINISTRADOR", "FARMACEUTICO") ? fetchJson(`${API_ROOT}/produtos/estoque-baixo`) : Promise.resolve([])
    ]);
    root.innerHTML = `
        <div class="module-grid">
            <section class="card">
                <div class="row space">
                    <h3>${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? "Cadastro e edicao" : "Consulta"}</h3>
                    <span class="tag">${products.length} itens</span>
                </div>
                ${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? productForm() : `<p class="muted">Seu perfil tem acesso de leitura ao catalogo.</p>`}
            </section>
            <section class="table-card">
                <div class="row space">
                    <h3>Estoque minimo</h3>
                    <span class="table-caption">Itens abaixo do limite</span>
                </div>
                ${renderSimpleTable(lowStock, ["nome", "quantidadeEstoque", "estoqueMinimo", "preco"], {
                    nome: "Produto",
                    quantidadeEstoque: "Atual",
                    estoqueMinimo: "Minimo",
                    preco: "Preco"
                }, (key, value) => key === "preco" ? escapeHtml(formatCurrency(value)) : escapeHtml(formatValue(value)))}
            </section>
        </div>
        <section class="table-card">
            <div class="table-toolbar row space">
                <div>
                    <h3>Catalogo</h3>
                    <p class="muted">Edite direto na grade ou filtre por categoria.</p>
                </div>
                <div class="toolbar">
                    <select id="products-category-filter" class="search-input">
                        <option value="">Todas as categorias</option>
                        ${CATEGORY_OPTIONS.map((item) => `<option value="${item}">${humanize(item)}</option>`).join("")}
                    </select>
                    <input id="products-search" class="search-input" type="search" placeholder="Buscar por nome ou codigo de barras">
                </div>
            </div>
            <div id="products-table">${renderProductsTable(products)}</div>
        </section>
    `;

    if (hasRole("ADMINISTRADOR", "FARMACEUTICO")) {
        document.getElementById("product-form").addEventListener("submit", submitProduct);
        document.getElementById("product-reset").addEventListener("click", resetProductForm);
    }
    document.getElementById("products-category-filter").addEventListener("change", applyProductFilters);
    document.getElementById("products-search").addEventListener("input", applyProductFilters);
    document.querySelectorAll("[data-edit-product]").forEach((button) => button.addEventListener("click", populateProductForm));
    document.querySelectorAll("[data-delete-product]").forEach((button) => button.addEventListener("click", deleteProduct));
}

function productForm() {
    return `
        <form id="product-form" class="stack">
            <input type="hidden" name="id">
            <div class="field-grid">
                <div class="field"><label>Nome</label><input name="nome" required></div>
                <div class="field"><label>Codigo de barras</label><input name="codigoBarras"></div>
                <div class="field"><label>Categoria</label><select name="categoria" required>${renderOptions(CATEGORY_OPTIONS)}</select></div>
                <div class="field"><label>Unidade de medida</label><input name="unidadeMedida" value="UN" required></div>
                <div class="field"><label>Estoque minimo</label><input name="estoqueMinimo" type="number" min="0" value="0"></div>
                <div class="field"><label>Preco</label><input name="preco" type="number" min="0.01" step="0.01" required></div>
            </div>
            <div class="field"><label>Descricao</label><textarea name="descricao"></textarea></div>
            <div class="actions">
                <button class="button" type="submit">Salvar produto</button>
                <button id="product-reset" class="ghost-button" type="button">Limpar</button>
            </div>
        </form>
    `;
}

function renderProductsTable(items) {
    if (!items.length) {
        return `<div class="empty-state"><div><h3>Nenhum produto encontrado</h3><p>Ajuste os filtros ou cadastre um item.</p></div></div>`;
    }
    return `
        <div class="table-scroll">
            <table>
                <thead>
                    <tr><th>Produto</th><th>Categoria</th><th>Estoque</th><th>Minimo</th><th>Preco</th><th>Status</th>${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? "<th>Acoes</th>" : ""}</tr>
                </thead>
                <tbody>
                    ${items.map((item) => `
                        <tr>
                            <td><strong>${escapeHtml(item.nome)}</strong><br><span class="hint">${escapeHtml(item.codigoBarras || "Sem codigo")}</span></td>
                            <td>${escapeHtml(humanize(item.categoria))}</td>
                            <td>${escapeHtml(formatValue(item.quantidadeEstoque))}</td>
                            <td>${escapeHtml(formatValue(item.estoqueMinimo))}</td>
                            <td>${escapeHtml(formatCurrency(item.preco))}</td>
                            <td>${item.ativo ? badge(item.abaixoDoEstoqueMinimo ? "ALERTA" : "ATIVO") : badge("INATIVO")}</td>
                            ${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? `<td><div class="row"><button type="button" class="ghost-button" data-edit-product="${item.id}">Editar</button>${hasRole("ADMINISTRADOR") ? `<button type="button" class="danger-button" data-delete-product="${item.id}">Desativar</button>` : ""}</div></td>` : ""}
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function applyProductFilters() {
    const category = document.getElementById("products-category-filter").value;
    const term = document.getElementById("products-search").value.trim().toLowerCase();
    const filtered = state.products.filter((item) => {
        const matchesCategory = !category || item.categoria === category;
        const matchesTerm = !term || item.nome.toLowerCase().includes(term) || String(item.codigoBarras || "").toLowerCase().includes(term);
        return matchesCategory && matchesTerm;
    });
    document.getElementById("products-table").innerHTML = renderProductsTable(filtered);
    document.querySelectorAll("[data-edit-product]").forEach((button) => button.addEventListener("click", populateProductForm));
    document.querySelectorAll("[data-delete-product]").forEach((button) => button.addEventListener("click", deleteProduct));
}

function populateProductForm(event) {
    const id = Number(event.currentTarget.dataset.editProduct);
    const product = state.products.find((item) => item.id === id);
    if (!product) {
        return;
    }
    const form = document.getElementById("product-form");
    form.elements.namedItem("id").value = product.id;
    form.nome.value = product.nome || "";
    form.codigoBarras.value = product.codigoBarras || "";
    form.categoria.value = product.categoria || CATEGORY_OPTIONS[0];
    form.unidadeMedida.value = product.unidadeMedida || "UN";
    form.estoqueMinimo.value = product.estoqueMinimo ?? 0;
    form.preco.value = product.preco ?? "";
    form.descricao.value = product.descricao || "";
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetProductForm() {
    const form = document.getElementById("product-form");
    if (form) {
        form.reset();
        form.elements.namedItem("id").value = "";
        form.unidadeMedida.value = "UN";
        form.estoqueMinimo.value = 0;
    }
}

async function submitProduct(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const payload = {
        nome: form.nome.value.trim(),
        descricao: form.descricao.value.trim(),
        codigoBarras: form.codigoBarras.value.trim(),
        categoria: form.categoria.value,
        unidadeMedida: form.unidadeMedida.value.trim(),
        estoqueMinimo: Number(form.estoqueMinimo.value || 0),
        preco: Number(form.preco.value)
    };
    const id = form.elements.namedItem("id").value;
    setBusy(true);
    try {
        await fetchJson(`${API_ROOT}/produtos${id ? `/${id}` : ""}`, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        setStatus("info", id ? "Produto atualizado." : "Produto cadastrado.");
        resetProductForm();
        await renderView();
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    } finally {
        setBusy(false);
    }
}

async function deleteProduct(event) {
    const id = event.currentTarget.dataset.deleteProduct;
    if (!window.confirm("Desativar este produto?")) {
        return;
    }
    setBusy(true);
    try {
        await fetchVoid(`${API_ROOT}/produtos/${id}`, { method: "DELETE" });
        setStatus("warn", "Produto desativado.");
        await renderView();
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    } finally {
        setBusy(false);
    }
}

async function renderStock(root) {
    const products = await fetchProducts();
    root.innerHTML = `
        <div class="module-grid">
            <section class="card">
                <h3>Entrada de estoque</h3>
                <form id="entry-form" class="stack">
                    <div class="field-grid">
                        <div class="field"><label>Produto</label><select name="produtoId" required>${renderProductOptions(products)}</select></div>
                        <div class="field"><label>Quantidade</label><input name="quantidade" type="number" min="1" required></div>
                        <div class="field"><label>Lote</label><input name="numeroLote" required></div>
                        <div class="field"><label>Validade</label><input name="dataValidade" type="date" min="${todayIso()}" required></div>
                    </div>
                    <div class="field"><label>Motivo</label><textarea name="motivo" placeholder="Reposicao, compra, ajuste..."></textarea></div>
                    <button class="button" type="submit">Registrar entrada</button>
                </form>
            </section>
            <section class="card">
                <h3>Saida de estoque</h3>
                <form id="exit-form" class="stack">
                    <div class="field-grid">
                        <div class="field"><label>Produto</label><select name="produtoId" required>${renderProductOptions(products)}</select></div>
                        <div class="field"><label>Quantidade</label><input name="quantidade" type="number" min="1" required></div>
                    </div>
                    <div class="field"><label>Motivo</label><textarea name="motivo" placeholder="Avaria, ajuste, uso interno..."></textarea></div>
                    <button class="secondary-button" type="submit">Registrar saida</button>
                </form>
            </section>
        </div>
        <div class="module-grid">
            <section class="table-card">
                <div class="row space">
                    <h3>Consulta de lotes</h3>
                    <div class="toolbar">
                        <select id="stock-product-lotes">${renderProductOptions(products)}</select>
                        <button class="ghost-button" type="button" data-stock-load="lotes">Carregar</button>
                    </div>
                </div>
                <div id="stock-lotes-table">${emptyInline("Selecione um produto para ver lotes.")}</div>
            </section>
            <section class="table-card">
                <div class="row space">
                    <h3>Movimentacoes</h3>
                    <div class="toolbar">
                        <select id="stock-product-movs">${renderProductOptions(products)}</select>
                        <button class="ghost-button" type="button" data-stock-load="movs">Carregar</button>
                    </div>
                </div>
                <div id="stock-movs-table">${emptyInline("Selecione um produto para ver movimentacoes.")}</div>
            </section>
        </div>
        <div class="module-grid">
            <section class="table-card">
                <div class="row space">
                    <h3>Lotes vencidos</h3>
                    <button type="button" class="ghost-button" data-stock-extra="vencidos">Atualizar</button>
                </div>
                <div id="stock-vencidos-table">${emptyInline("Nenhuma consulta realizada.")}</div>
            </section>
            <section class="table-card">
                <div class="row space">
                    <h3>A vencer</h3>
                    <div class="toolbar">
                        <input id="dias-a-vencer" class="search-input" type="number" value="30" min="1">
                        <button type="button" class="ghost-button" data-stock-extra="avencer">Consultar</button>
                    </div>
                </div>
                <div id="stock-avencer-table">${emptyInline("Nenhuma consulta realizada.")}</div>
            </section>
        </div>
    `;
    document.getElementById("entry-form").addEventListener("submit", submitStockEntry);
    document.getElementById("exit-form").addEventListener("submit", submitStockExit);
    document.querySelector('[data-stock-load="lotes"]').addEventListener("click", () => loadStockTable("lotes"));
    document.querySelector('[data-stock-load="movs"]').addEventListener("click", () => loadStockTable("movs"));
    document.querySelector('[data-stock-extra="vencidos"]').addEventListener("click", loadExpiredLots);
    document.querySelector('[data-stock-extra="avencer"]').addEventListener("click", loadExpiringLots);
}

async function submitStockEntry(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await submitJsonForm(`${API_ROOT}/estoque/entrada`, {
        produtoId: Number(form.produtoId.value),
        quantidade: Number(form.quantidade.value),
        numeroLote: form.numeroLote.value.trim(),
        dataValidade: form.dataValidade.value,
        motivo: form.motivo.value.trim()
    }, "Entrada registrada.");
    form.reset();
}

async function submitStockExit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await submitJsonForm(`${API_ROOT}/estoque/saida`, {
        produtoId: Number(form.produtoId.value),
        quantidade: Number(form.quantidade.value),
        motivo: form.motivo.value.trim()
    }, "Saida registrada.");
    form.reset();
}

async function loadStockTable(kind) {
    const productId = document.getElementById(kind === "lotes" ? "stock-product-lotes" : "stock-product-movs").value;
    const target = document.getElementById(kind === "lotes" ? "stock-lotes-table" : "stock-movs-table");
    target.innerHTML = '<div class="loader"></div>';
    try {
        const data = await fetchJson(`${API_ROOT}/estoque/${kind === "lotes" ? "lotes" : "movimentacoes"}/${productId}`);
        target.innerHTML = kind === "lotes"
            ? renderSimpleTable(data, ["numeroLote", "dataValidade", "quantidadeAtual", "vencido"], {
                numeroLote: "Lote",
                dataValidade: "Validade",
                quantidadeAtual: "Atual",
                vencido: "Status"
            }, (key, value) => key === "vencido" ? badge(value ? "VENCIDO" : "OK") : escapeHtml(formatValue(value)))
            : renderSimpleTable(data, ["tipo", "quantidade", "usuarioResponsavel", "dataMovimentacao"], {
                tipo: "Tipo",
                quantidade: "Qtd",
                usuarioResponsavel: "Responsavel",
                dataMovimentacao: "Data"
            }, (key, value) => key === "tipo" ? badge(value) : escapeHtml(formatValue(value)));
    } catch (error) {
        target.innerHTML = emptyInline(getErrorMessage(error));
    }
}

async function loadExpiredLots() {
    const target = document.getElementById("stock-vencidos-table");
    target.innerHTML = '<div class="loader"></div>';
    try {
        const data = await fetchJson(`${API_ROOT}/estoque/lotes/vencidos`);
        target.innerHTML = renderSimpleTable(data, ["nomeProduto", "numeroLote", "dataValidade", "quantidadeAtual"], {
            nomeProduto: "Produto",
            numeroLote: "Lote",
            dataValidade: "Validade",
            quantidadeAtual: "Qtd"
        });
    } catch (error) {
        target.innerHTML = emptyInline(getErrorMessage(error));
    }
}

async function loadExpiringLots() {
    const days = document.getElementById("dias-a-vencer").value || 30;
    const target = document.getElementById("stock-avencer-table");
    target.innerHTML = '<div class="loader"></div>';
    try {
        const data = await fetchJson(`${API_ROOT}/estoque/lotes/a-vencer?dias=${encodeURIComponent(days)}`);
        target.innerHTML = renderSimpleTable(data, ["nomeProduto", "numeroLote", "dataValidade", "quantidadeAtual"], {
            nomeProduto: "Produto",
            numeroLote: "Lote",
            dataValidade: "Validade",
            quantidadeAtual: "Qtd"
        });
    } catch (error) {
        target.innerHTML = emptyInline(getErrorMessage(error));
    }
}

async function renderSales(root) {
    const [products, promotions, customers, sales] = await Promise.all([
        fetchProducts(),
        hasRole("ADMINISTRADOR", "FARMACEUTICO") ? fetchJson(`${API_ROOT}/promocoes/vigentes`) : Promise.resolve([]),
        fetchCustomerListSafe(),
        hasRole("ADMINISTRADOR", "FARMACEUTICO") ? fetchJson(`${API_ROOT}/vendas`) : Promise.resolve([])
    ]);
    state.promotions = promotions;
    state.customers = customers;
    root.innerHTML = `
        <div class="module-grid">
            <section class="card">
                <div class="row space"><h3>Nova venda</h3><button type="button" class="ghost-button" data-action="add-sale-item">Adicionar item</button></div>
                <form id="sale-form" class="stack">
                    <div id="sale-items" class="sale-items">${renderSaleItemRows(products)}</div>
                    <div class="field-grid">
                        <div class="field"><label>Cliente</label><select name="clienteId"><option value="">Consumidor final</option>${customers.map((item) => `<option value="${item.usuarioId || item.id}">${escapeHtml(item.nome)}</option>`).join("")}</select></div>
                        <div class="field"><label>Forma de pagamento</label><select name="formaPagamento" required>${renderOptions(PAYMENT_OPTIONS)}</select></div>
                        <div class="field"><label>Promocao</label><select name="promocaoId"><option value="">Sem promocao</option>${promotions.map((item) => `<option value="${item.id}">${escapeHtml(item.nome)}</option>`).join("")}</select></div>
                        <div class="field"><label>Pontos para resgatar</label><input name="pontosParaResgatar" type="number" min="0" value="0"></div>
                        <div class="field"><label>Valor pago</label><input name="valorPago" type="number" min="0.01" step="0.01" required></div>
                    </div>
                    <div class="field"><label>Observacoes</label><textarea name="observacoes"></textarea></div>
                    <button class="button" type="submit">Registrar venda</button>
                </form>
            </section>
            <section class="card">
                <h3>Cupom atual</h3>
                <div id="coupon-preview">${state.saleCoupon ? `<div class="pre-box">${escapeHtml(state.saleCoupon)}</div>` : `<p class="muted">O cupom em texto simples aparece aqui apos a venda.</p>`}</div>
            </section>
        </div>
        <section class="table-card">
            <div class="row space"><h3>Historico de vendas</h3><span class="table-caption">Listagem disponivel para perfis gerenciais</span></div>
            ${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? renderSalesTable(sales) : emptyInline("Seu perfil pode cadastrar e consultar venda por id/cupom, mas a listagem completa e restrita pelo backend.")}
        </section>
    `;
    document.getElementById("sale-form").addEventListener("submit", submitSale);
    document.querySelectorAll("[data-remove-sale-item]").forEach((button) => button.addEventListener("click", removeSaleItemRow));
    document.querySelectorAll("[data-cupom-id]").forEach((button) => button.addEventListener("click", loadCoupon));
    document.querySelectorAll("[data-cancel-sale]").forEach((button) => button.addEventListener("click", cancelSale));
}

function renderSaleItemRows(products) {
    return state.saleItems.map((item, index) => `
        <div class="sale-item-row">
            <div class="field"><label>Produto</label><select data-sale-produto="${index}">${renderProductOptions(products, item.produtoId)}</select></div>
            <div class="field"><label>Qtd</label><input data-sale-quantidade="${index}" type="number" min="1" value="${item.quantidade}"></div>
            <button type="button" class="danger-button" data-remove-sale-item="${index}">Remover</button>
        </div>
    `).join("");
}

function addSaleItemRow() {
    state.saleItems.push({ produtoId: "", quantidade: 1 });
    render();
}

function removeSaleItemRow(event) {
    if (state.saleItems.length === 1) {
        return;
    }
    const index = Number(event.currentTarget.dataset.removeSaleItem);
    state.saleItems.splice(index, 1);
    render();
}

async function submitSale(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const itemRows = Array.from(document.querySelectorAll("[data-sale-produto]"));
    const itens = itemRows.map((select) => {
        const index = Number(select.dataset.saleProduto);
        const quantityField = document.querySelector(`[data-sale-quantidade="${index}"]`);
        return {
            produtoId: Number(select.value),
            quantidade: Number(quantityField.value)
        };
    }).filter((item) => item.produtoId && item.quantidade > 0);

    await submitJsonForm(`${API_ROOT}/vendas`, {
        clienteId: form.clienteId.value ? Number(form.clienteId.value) : null,
        itens,
        formaPagamento: form.formaPagamento.value,
        valorPago: Number(form.valorPago.value),
        promocaoId: form.promocaoId.value ? Number(form.promocaoId.value) : null,
        pontosParaResgatar: Number(form.pontosParaResgatar.value || 0),
        observacoes: form.observacoes.value.trim()
    }, "Venda registrada.", async (result) => {
        if (result?.id) {
            state.saleCoupon = await fetchText(`${API_ROOT}/vendas/${result.id}/cupom`);
        }
        state.saleItems = [{ produtoId: "", quantidade: 1 }];
    });
    await renderView();
}

function renderSalesTable(items, options = {}) {
    const showCoupon = options.showCoupon ?? hasRole("ADMINISTRADOR", "FARMACEUTICO", "ATENDENTE");
    const allowCancel = options.allowCancel ?? hasRole("ADMINISTRADOR", "FARMACEUTICO");
    if (!items.length) {
        return emptyInline("Nenhuma venda registrada.");
    }
    return `
        <div class="table-scroll">
            <table>
                <thead><tr><th>Cupom</th><th>Cliente</th><th>Total</th><th>Pagamento</th><th>Status</th><th>Data</th>${showCoupon || allowCancel ? "<th>Acoes</th>" : ""}</tr></thead>
                <tbody>
                    ${items.map((item) => `
                        <tr>
                            <td>${escapeHtml(item.numeroCupom)}</td>
                            <td>${escapeHtml(item.clienteNome || "Consumidor Final")}</td>
                            <td>${escapeHtml(formatCurrency(item.totalFinal))}</td>
                            <td>${escapeHtml(humanize(item.formaPagamento))}</td>
                            <td>${badge(item.status)}</td>
                            <td>${escapeHtml(formatValue(item.dataVenda))}</td>
                            ${showCoupon || allowCancel ? `<td><div class="row">${showCoupon ? `<button type="button" class="ghost-button" data-cupom-id="${item.id}">Cupom</button>` : ""}${allowCancel && item.status === "CONCLUIDA" ? `<button type="button" class="danger-button" data-cancel-sale="${item.id}">Cancelar</button>` : ""}</div></td>` : ""}
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

async function loadCoupon(event) {
    const id = event.currentTarget.dataset.cupomId;
    try {
        const coupon = await fetchText(`${API_ROOT}/vendas/${id}/cupom`);
        openModal("Cupom da venda", `<div class="pre-box">${escapeHtml(coupon)}</div>`);
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    }
}

async function cancelSale(event) {
    const id = event.currentTarget.dataset.cancelSale;
    if (!window.confirm("Cancelar esta venda?")) {
        return;
    }
    await submitJsonForm(`${API_ROOT}/vendas/${id}/cancelar`, null, "Venda cancelada.", null, "POST");
    await renderView();
}

async function renderPromotions(root) {
    const [allPromotions, activePromotions] = await Promise.all([
        hasRole("ADMINISTRADOR", "FARMACEUTICO") ? fetchJson(`${API_ROOT}/promocoes`) : Promise.resolve([]),
        fetchJson(`${API_ROOT}/promocoes/vigentes`)
    ]);
    state.promotions = allPromotions;
    root.innerHTML = `
        <div class="module-grid">
            ${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? `
                <section class="card">
                    <h3>Nova promocao</h3>
                    <form id="promotion-form" class="stack">
                        <div class="field-grid">
                            <div class="field"><label>Nome</label><input name="nome" required></div>
                            <div class="field"><label>Tipo de desconto</label><select name="tipoDesconto" required>${renderOptions(DISCOUNT_OPTIONS)}</select></div>
                            <div class="field"><label>Valor</label><input name="valorDesconto" type="number" min="0.01" step="0.01" required></div>
                            <div class="field"><label>Inicio</label><input name="dataInicio" type="date" required></div>
                            <div class="field"><label>Fim</label><input name="dataFim" type="date" required></div>
                        </div>
                        <div class="field"><label>Descricao</label><textarea name="descricao"></textarea></div>
                        <button class="button" type="submit">Salvar promocao</button>
                    </form>
                </section>
            ` : ""}
            <section class="table-card">
                <div class="row space"><h3>Promocoes vigentes</h3><span class="tag ok">${activePromotions.length} ativas</span></div>
                ${renderSimpleTable(activePromotions, ["nome", "tipoDesconto", "valorDesconto", "dataFim"], {
                    nome: "Campanha",
                    tipoDesconto: "Tipo",
                    valorDesconto: "Valor",
                    dataFim: "Termina em"
                }, (key, value) => key === "valorDesconto" ? escapeHtml(formatCurrency(value)) : escapeHtml(formatValue(value)))}
            </section>
        </div>
        ${hasRole("ADMINISTRADOR", "FARMACEUTICO") ? `
            <section class="table-card">
                <div class="row space"><h3>Todas as promocoes</h3><span class="table-caption">Inative quando necessario</span></div>
                <div class="table-scroll">
                    <table>
                        <thead><tr><th>Nome</th><th>Tipo</th><th>Valor</th><th>Periodo</th><th>Status</th><th>Acoes</th></tr></thead>
                        <tbody>
                            ${allPromotions.map((item) => `
                                <tr>
                                    <td>${escapeHtml(item.nome)}</td>
                                    <td>${escapeHtml(humanize(item.tipoDesconto))}</td>
                                    <td>${escapeHtml(formatCurrency(item.valorDesconto))}</td>
                                    <td>${escapeHtml(formatValue(item.dataInicio))} ate ${escapeHtml(formatValue(item.dataFim))}</td>
                                    <td>${badge(item.vigente ? "VIGENTE" : item.ativa ? "ATIVA" : "INATIVA")}</td>
                                    <td>${hasRole("ADMINISTRADOR") ? `<button type="button" class="danger-button" data-delete-promotion="${item.id}">Desativar</button>` : ""}</td>
                                </tr>
                            `).join("")}
                        </tbody>
                    </table>
                </div>
            </section>
        ` : ""}
    `;
    if (hasRole("ADMINISTRADOR", "FARMACEUTICO")) {
        document.getElementById("promotion-form")?.addEventListener("submit", submitPromotion);
        document.querySelectorAll("[data-delete-promotion]").forEach((button) => button.addEventListener("click", deletePromotion));
    }
}

async function submitPromotion(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await submitJsonForm(`${API_ROOT}/promocoes`, {
        nome: form.nome.value.trim(),
        descricao: form.descricao.value.trim(),
        tipoDesconto: form.tipoDesconto.value,
        valorDesconto: Number(form.valorDesconto.value),
        dataInicio: form.dataInicio.value,
        dataFim: form.dataFim.value
    }, "Promocao cadastrada.");
    form.reset();
    await renderView();
}

async function deletePromotion(event) {
    const id = event.currentTarget.dataset.deletePromotion;
    if (!window.confirm("Desativar esta promocao?")) {
        return;
    }
    try {
        setBusy(true);
        await fetchVoid(`${API_ROOT}/promocoes/${id}`, { method: "DELETE" });
        setStatus("warn", "Promocao desativada.");
        await renderView();
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    } finally {
        setBusy(false);
    }
}

async function renderReservations(root) {
    const [products, reservations] = await Promise.all([
        fetchProducts(),
        fetchJson(`${API_ROOT}/reservas${hasRole("CLIENTE") ? "/minhas" : hasRole("ADMINISTRADOR", "FARMACEUTICO") ? "" : "/minhas"}`)
    ]);
    root.innerHTML = `
        <div class="module-grid">
            <section class="card">
                <h3>Nova reserva</h3>
                <form id="reservation-form" class="stack">
                    <div class="field-grid">
                        <div class="field"><label>Produto</label><select name="produtoId" required>${renderProductOptions(products)}</select></div>
                        <div class="field"><label>Quantidade</label><input name="quantidade" type="number" min="1" required></div>
                    </div>
                    <div class="field"><label>Observacoes</label><textarea name="observacoes"></textarea></div>
                    <button class="button" type="submit">Criar reserva</button>
                </form>
            </section>
            <section class="card">
                <h3>Leitura rapida</h3>
                <p class="muted">Use confirmar quando a retirada for concluida. Cancelar mantem o historico operacional sem remover o registro.</p>
                <div class="badge-row">
                    <span class="tag ok">Disponiveis: ${reservations.filter((item) => item.status === "DISPONIVEL").length}</span>
                    <span class="tag warn">Pendentes: ${reservations.filter((item) => item.status === "PENDENTE").length}</span>
                    <span class="tag danger">Expiradas: ${reservations.filter((item) => item.status === "EXPIRADA").length}</span>
                </div>
            </section>
        </div>
        <section class="table-card">
            <div class="row space"><h3>${hasRole("CLIENTE") ? "Minhas reservas" : "Reservas"}</h3><span class="table-caption">Acoes variam conforme o backend e o perfil</span></div>
            <div class="table-scroll">
                <table>
                    <thead><tr><th>Produto</th><th>Cliente</th><th>Quantidade</th><th>Status</th><th>Expiracao</th><th>Acoes</th></tr></thead>
                    <tbody>
                        ${reservations.map((item) => `
                            <tr>
                                <td>${escapeHtml(item.nomeProduto)}</td>
                                <td>${escapeHtml(item.clienteNome || "- ")}</td>
                                <td>${escapeHtml(formatValue(item.quantidade))}</td>
                                <td>${badge(item.status)}</td>
                                <td>${escapeHtml(formatValue(item.dataExpiracao))}</td>
                                <td>
                                    <div class="row">
                                        ${!["CONFIRMADA", "CANCELADA", "EXPIRADA"].includes(item.status) ? `<button type="button" class="ghost-button" data-confirm-reservation="${item.id}">Confirmar</button>` : ""}
                                        ${!["CANCELADA", "CONFIRMADA", "EXPIRADA"].includes(item.status) ? `<button type="button" class="danger-button" data-cancel-reservation="${item.id}">Cancelar</button>` : ""}
                                    </div>
                                </td>
                            </tr>
                        `).join("")}
                    </tbody>
                </table>
            </div>
        </section>
    `;
    document.getElementById("reservation-form").addEventListener("submit", submitReservation);
    document.querySelectorAll("[data-confirm-reservation]").forEach((button) => button.addEventListener("click", confirmReservation));
    document.querySelectorAll("[data-cancel-reservation]").forEach((button) => button.addEventListener("click", cancelReservation));
}

async function submitReservation(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await submitJsonForm(`${API_ROOT}/reservas`, {
        produtoId: Number(form.produtoId.value),
        quantidade: Number(form.quantidade.value),
        observacoes: form.observacoes.value.trim()
    }, "Reserva criada.");
    form.reset();
    await renderView();
}

async function confirmReservation(event) {
    await submitJsonForm(`${API_ROOT}/reservas/${event.currentTarget.dataset.confirmReservation}/confirmar`, null, "Reserva confirmada.", null, "POST");
    await renderView();
}

async function cancelReservation(event) {
    await submitJsonForm(`${API_ROOT}/reservas/${event.currentTarget.dataset.cancelReservation}/cancelar`, null, "Reserva cancelada.", null, "POST");
    await renderView();
}

async function renderCustomers(root) {
    if (hasRole("CLIENTE")) {
        root.innerHTML = `
            <div class="module-grid">
                <section class="card"><div id="my-profile-area">${emptyInline("Carregue seu perfil para editar dados cadastrais.")}</div><button type="button" class="button" data-action="load-my-profile">Carregar perfil</button></section>
                <section class="table-card"><div id="my-history-area">${emptyInline("Carregue seu historico de compras.")}</div><button type="button" class="ghost-button" data-action="load-my-history">Carregar historico</button></section>
            </div>
        `;
        bindGlobalEvents();
        return;
    }

    const customers = await fetchCustomerListSafe();
    state.customers = customers;
    root.innerHTML = `
        <div class="module-grid">
            <section class="table-card">
                <div class="row space"><h3>Clientes</h3><span class="tag">${customers.length} registros</span></div>
                ${renderSimpleTable(customers, ["nome", "email", "nivel", "pontosAcumulados"], {
                    nome: "Nome",
                    email: "Email",
                    nivel: "Nivel",
                    pontosAcumulados: "Pontos"
                })}
            </section>
            <section class="card">
                <h3>Historico por cliente</h3>
                <div class="field-grid">
                    <div class="field"><label>Cliente</label><select id="customer-history-id">${customers.map((item) => `<option value="${item.usuarioId || item.id}">${escapeHtml(item.nome)}</option>`).join("")}</select></div>
                </div>
                <div class="actions"><button type="button" class="button" id="load-customer-history">Carregar historico</button></div>
                <div id="customer-history-table">${emptyInline("Selecione um cliente para ver compras anteriores.")}</div>
            </section>
        </div>
    `;
    document.getElementById("load-customer-history").addEventListener("click", loadCustomerHistory);
}

async function loadMyProfile() {
    const area = document.getElementById("my-profile-area");
    area.innerHTML = '<div class="loader"></div>';
    try {
        const profile = await fetchJson(`${API_ROOT}/clientes/meu-perfil`);
        area.innerHTML = `
            <div class="stack">
                <div class="badge-row">
                    <span class="tag ok">${escapeHtml(profile.nivel || "BRONZE")}</span>
                    <span class="tag">Pontos: ${escapeHtml(formatValue(profile.pontosAcumulados))}</span>
                    <span class="tag">Desconto: ${escapeHtml(formatCurrency(profile.descontoDisponivel || 0))}</span>
                </div>
                <form id="my-profile-form" class="stack">
                    <div class="field-grid">
                        <div class="field"><label>CPF</label><input name="cpf" value="${escapeHtml(profile.cpf || "")}"></div>
                        <div class="field"><label>Telefone</label><input name="telefone" value="${escapeHtml(profile.telefone || "")}"></div>
                        <div class="field"><label>Data de nascimento</label><input name="dataNascimento" type="date" value="${profile.dataNascimento || ""}"></div>
                    </div>
                    <div class="field"><label>Endereco</label><textarea name="endereco">${escapeHtml(profile.endereco || "")}</textarea></div>
                    <button class="button" type="submit">Salvar perfil</button>
                </form>
            </div>
        `;
        document.getElementById("my-profile-form").addEventListener("submit", saveMyProfile);
    } catch (error) {
        area.innerHTML = emptyInline(getErrorMessage(error));
    }
}

async function saveMyProfile(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await submitJsonForm(`${API_ROOT}/clientes/meu-perfil`, {
        cpf: form.cpf.value.trim(),
        telefone: form.telefone.value.trim(),
        dataNascimento: form.dataNascimento.value || null,
        endereco: form.endereco.value.trim()
    }, "Perfil atualizado.", null, "PUT");
    await loadMyProfile();
}

async function loadMyHistory() {
    const area = document.getElementById("my-history-area");
    area.innerHTML = '<div class="loader"></div>';
    try {
        const history = await fetchJson(`${API_ROOT}/clientes/meu-perfil/historico`);
        area.innerHTML = `<h3>Historico de compras</h3>${renderSalesTable(history, { showCoupon: false, allowCancel: false })}`;
    } catch (error) {
        area.innerHTML = emptyInline(getErrorMessage(error));
    }
}

async function loadCustomerHistory() {
    const id = document.getElementById("customer-history-id").value;
    const area = document.getElementById("customer-history-table");
    area.innerHTML = '<div class="loader"></div>';
    try {
        const history = await fetchJson(`${API_ROOT}/clientes/${id}/historico`);
        area.innerHTML = renderSalesTable(history);
        document.querySelectorAll("[data-cupom-id]").forEach((button) => button.addEventListener("click", loadCoupon));
        document.querySelectorAll("[data-cancel-sale]").forEach((button) => button.addEventListener("click", cancelSale));
    } catch (error) {
        area.innerHTML = emptyInline(getErrorMessage(error));
    }
}

async function renderUsers(root) {
    const users = await fetchUserListSafe();
    state.users = users;
    root.innerHTML = `
        <div class="module-grid">
            ${hasRole("ADMINISTRADOR") ? `
                <section class="card">
                    <h3>Novo usuario</h3>
                    <form id="user-form" class="stack">
                        <div class="field-grid">
                            <div class="field"><label>Nome</label><input name="nome" required></div>
                            <div class="field"><label>Email</label><input name="email" type="email" required></div>
                            <div class="field"><label>Senha</label><input name="senha" type="password" minlength="6" required></div>
                            <div class="field"><label>Perfil</label><select name="perfil" required>${renderOptions(PROFILE_OPTIONS)}</select></div>
                        </div>
                        <button class="button" type="submit">Cadastrar usuario</button>
                    </form>
                </section>
            ` : `<section class="card"><h3>Permissao de gestao limitada</h3><p class="muted">Seu perfil pode consultar usuarios, mas o cadastro e restrito a administradores.</p></section>`}
            <section class="table-card">
                <div class="row space"><h3>Usuarios</h3><span class="tag">${users.length} cadastrados</span></div>
                <div class="table-scroll">
                    <table>
                        <thead><tr><th>Nome</th><th>Email</th><th>Perfil</th><th>Status</th>${hasRole("ADMINISTRADOR") ? "<th>Acoes</th>" : ""}</tr></thead>
                        <tbody>
                            ${users.map((item) => `
                                <tr>
                                    <td>${escapeHtml(item.nome)}</td>
                                    <td>${escapeHtml(item.email)}</td>
                                    <td>${escapeHtml(humanize(item.perfil))}</td>
                                    <td>${badge(item.ativo ? "ATIVO" : "INATIVO")}</td>
                                    ${hasRole("ADMINISTRADOR") ? `<td>${item.ativo ? `<button type="button" class="danger-button" data-delete-user="${item.id}">Desativar</button>` : ""}</td>` : ""}
                                </tr>
                            `).join("")}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    `;
    document.getElementById("user-form")?.addEventListener("submit", submitUser);
    document.querySelectorAll("[data-delete-user]").forEach((button) => button.addEventListener("click", deleteUser));
}

async function submitUser(event) {
    event.preventDefault();
    const form = event.currentTarget;
    await submitJsonForm(`${API_ROOT}/usuarios`, {
        nome: form.nome.value.trim(),
        email: form.email.value.trim(),
        senha: form.senha.value,
        perfil: form.perfil.value
    }, "Usuario cadastrado.");
    form.reset();
    await renderView();
}

async function deleteUser(event) {
    const id = event.currentTarget.dataset.deleteUser;
    if (!window.confirm("Desativar este usuario?")) {
        return;
    }
    try {
        setBusy(true);
        await fetchVoid(`${API_ROOT}/usuarios/${id}`, { method: "DELETE" });
        setStatus("warn", "Usuario desativado.");
        await renderView();
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    } finally {
        setBusy(false);
    }
}

async function renderNotifications(root) {
    const notifications = await fetchJson(`${API_ROOT}/notificacoes`);
    root.innerHTML = `
        <div class="module-grid">
            <section class="card">
                <h3>Resumo</h3>
                <div class="badge-row">
                    <span class="tag">Total: ${notifications.length}</span>
                    <span class="tag warn">Nao lidas: ${notifications.filter((item) => !item.lida).length}</span>
                </div>
                <p class="muted">Use o painel para limpar a fila e acompanhar alertas de reservas.</p>
                <button type="button" class="button" data-action="mark-all-notifications">Marcar todas como lidas</button>
            </section>
            <section class="table-card">
                <h3>Fila de notificacoes</h3>
                <div class="table-scroll">
                    <table>
                        <thead><tr><th>Titulo</th><th>Mensagem</th><th>Data</th><th>Status</th><th>Acoes</th></tr></thead>
                        <tbody>
                            ${notifications.map((item) => `
                                <tr>
                                    <td>${escapeHtml(item.titulo)}</td>
                                    <td>${escapeHtml(item.mensagem)}</td>
                                    <td>${escapeHtml(formatValue(item.dataCriacao))}</td>
                                    <td>${badge(item.lida ? "LIDA" : "NOVA")}</td>
                                    <td>${!item.lida ? `<button type="button" class="ghost-button" data-mark-notification="${item.id}">Marcar lida</button>` : ""}</td>
                                </tr>
                            `).join("")}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    `;
    bindGlobalEvents();
    document.querySelectorAll("[data-mark-notification]").forEach((button) => button.addEventListener("click", markNotification));
}

async function markNotification(event) {
    await submitJsonForm(`${API_ROOT}/notificacoes/${event.currentTarget.dataset.markNotification}/marcar-lida`, null, "Notificacao marcada como lida.", null, "POST");
    await renderView();
}

async function markAllNotifications() {
    await submitJsonForm(`${API_ROOT}/notificacoes/marcar-todas-lidas`, null, "Notificacoes atualizadas.", null, "POST");
    await renderView();
}

async function renderReports(root) {
    root.innerHTML = `
        <div class="module-grid">
            <section class="card">
                <h3>Relatorio de vendas</h3>
                <form id="sales-report-form" class="stack">
                    <div class="field-grid">
                        <div class="field"><label>Data inicial</label><input name="dataInicio" type="date" required></div>
                        <div class="field"><label>Data final</label><input name="dataFim" type="date" required></div>
                    </div>
                    <div class="actions">
                        <button class="button" type="submit">Visualizar JSON</button>
                        <button class="ghost-button" type="button" data-report-download="vendas-excel">Baixar Excel</button>
                        <button class="ghost-button" type="button" data-report-download="vendas-pdf">Baixar PDF</button>
                    </div>
                </form>
            </section>
            <section class="card">
                <h3>Relatorio de estoque</h3>
                <div class="actions">
                    <button class="secondary-button" type="button" id="load-stock-report">Visualizar JSON</button>
                    <button class="ghost-button" type="button" data-report-download="estoque-excel">Baixar Excel</button>
                    <button class="ghost-button" type="button" data-report-download="estoque-pdf">Baixar PDF</button>
                </div>
            </section>
        </div>
        <div class="module-grid">
            <section class="table-card"><h3>Saida estruturada</h3><div id="report-output">${emptyInline("Execute um relatorio para visualizar a resposta JSON.")}</div></section>
            <section class="card">
                <h3>Uso</h3>
                <p class="muted">As exportacoes usam o mesmo backend atual. O preview JSON ajuda a validar o periodo antes de baixar os arquivos finais.</p>
            </section>
        </div>
    `;
    document.getElementById("sales-report-form").addEventListener("submit", loadSalesReport);
    document.getElementById("load-stock-report").addEventListener("click", loadStockReportJson);
    document.querySelectorAll("[data-report-download]").forEach((button) => button.addEventListener("click", downloadReport));
}

async function loadSalesReport(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const query = new URLSearchParams({ dataInicio: form.dataInicio.value, dataFim: form.dataFim.value }).toString();
    const data = await fetchJson(`${API_ROOT}/relatorios/vendas?${query}`);
    document.getElementById("report-output").innerHTML = `<div class="pre-box">${escapeHtml(JSON.stringify(data, null, 2))}</div>`;
}

async function loadStockReportJson() {
    const data = await fetchJson(`${API_ROOT}/relatorios/estoque`);
    document.getElementById("report-output").innerHTML = `<div class="pre-box">${escapeHtml(JSON.stringify(data, null, 2))}</div>`;
}

async function downloadReport(event) {
    const action = event.currentTarget.dataset.reportDownload;
    let url = "";
    let fileName = "";
    if (action.startsWith("vendas")) {
        const form = document.getElementById("sales-report-form");
        if (!form.dataInicio.value || !form.dataFim.value) {
            setStatus("warn", "Informe data inicial e final para exportar vendas.");
            return;
        }
        const query = new URLSearchParams({ dataInicio: form.dataInicio.value, dataFim: form.dataFim.value }).toString();
        url = `${API_ROOT}/relatorios/vendas/${action.endsWith("excel") ? "excel" : "pdf"}?${query}`;
        fileName = `relatorio-vendas.${action.endsWith("excel") ? "xlsx" : "pdf"}`;
    } else {
        url = `${API_ROOT}/relatorios/estoque/${action.endsWith("excel") ? "excel" : "pdf"}`;
        fileName = `relatorio-estoque.${action.endsWith("excel") ? "xlsx" : "pdf"}`;
    }
    try {
        const blob = await fetchBlob(url);
        const objectUrl = URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = objectUrl;
        anchor.download = fileName;
        anchor.click();
        URL.revokeObjectURL(objectUrl);
    } catch (error) {
        setStatus("error", getErrorMessage(error));
    }
}

function openModal(title, html) {
    document.getElementById("modal-title").textContent = title;
    document.getElementById("modal-content").innerHTML = html;
    document.getElementById("modal").classList.add("open");
}

function closeModal() {
    document.getElementById("modal")?.classList.remove("open");
}

function emptyInline(message) {
    return `<div class="empty-state"><div><p>${escapeHtml(message)}</p></div></div>`;
}

function metricCard(title, value, hint) {
    return `<section class="card metric"><span class="muted">${escapeHtml(title)}</span><strong>${escapeHtml(formatValue(value))}</strong><span class="delta">${escapeHtml(hint)}</span></section>`;
}

function renderSimpleTable(items, keys, labels, formatter) {
    if (!items || !items.length) {
        return emptyInline("Nenhum dado disponivel.");
    }
    return `
        <div class="table-scroll">
            <table>
                <thead><tr>${keys.map((key) => `<th>${escapeHtml(labels[key] || humanize(key))}</th>`).join("")}</tr></thead>
                <tbody>
                    ${items.map((item) => `<tr>${keys.map((key) => `<td>${formatter ? formatter(key, item[key]) : escapeHtml(formatValue(item[key]))}</td>`).join("")}</tr>`).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderOptions(values) {
    return values.map((value) => `<option value="${value}">${humanize(value)}</option>`).join("");
}

function renderProductOptions(products, selected = "") {
    return products.map((product) => `<option value="${product.id}" ${String(product.id) === String(selected) ? "selected" : ""}>${escapeHtml(product.nome)} (${escapeHtml(formatValue(product.quantidadeEstoque))})</option>`).join("");
}

function badge(value) {
    const normalized = String(value || "");
    const level = /ATIVO|OK|VIGENTE|CONFIRMADA|DISPONIVEL|LIDA|NOVA/.test(normalized) ? "ok"
        : /ALERTA|PENDENTE|EXPIRADA/.test(normalized) ? "warn"
        : /INATIVO|CANCELADA|VENCIDO/.test(normalized) ? "danger"
        : "";
    return `<span class="tag ${level}">${escapeHtml(humanize(normalized))}</span>`;
}

function humanize(value) {
    return String(value || "")
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/\b\w/g, (char) => char.toUpperCase());
}

function formatCurrency(value) {
    const number = Number(value || 0);
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(number);
}

function formatValue(value) {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    if (typeof value === "number") {
        return Number.isInteger(value) ? String(value) : String(value).replace(".", ",");
    }
    if (typeof value === "boolean") {
        return value ? "Sim" : "Nao";
    }
    if (/^\d{4}-\d{2}-\d{2}$/.test(String(value))) {
        return new Date(`${value}T00:00:00`).toLocaleDateString("pt-BR");
    }
    if (/^\d{4}-\d{2}-\d{2}T/.test(String(value))) {
        return new Date(value).toLocaleString("pt-BR");
    }
    return String(value);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function todayIso() {
    return new Date().toISOString().slice(0, 10);
}

async function fetchProducts() {
    const data = await fetchJson(`${API_ROOT}/produtos`);
    state.products = data;
    state.productMap = new Map(data.map((item) => [item.id, item]));
    return data;
}

async function fetchCustomerListSafe() {
    if (!hasRole("ADMINISTRADOR", "FARMACEUTICO")) {
        return [];
    }
    return fetchJson(`${API_ROOT}/clientes`);
}

async function fetchUserListSafe() {
    if (!hasRole("ADMINISTRADOR", "FARMACEUTICO")) {
        return [];
    }
    return fetchJson(`${API_ROOT}/usuarios`);
}

async function submitJsonForm(url, payload, successMessage, onSuccess, method = "POST") {
    try {
        setBusy(true);
        const response = await fetchJson(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: payload === null ? null : JSON.stringify(payload)
        });
        if (onSuccess) {
            await onSuccess(response);
        }
        setStatus("info", successMessage);
        return response;
    } catch (error) {
        setStatus("error", getErrorMessage(error));
        throw error;
    } finally {
        setBusy(false);
    }
}

async function fetchJson(url, options = {}, withAuth = true) {
    const response = await fetch(url, withAuth ? withAuthorization(options) : options);
    if (!response.ok) {
        throw await buildError(response);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

async function fetchVoid(url, options = {}) {
    const response = await fetch(url, withAuthorization(options));
    if (!response.ok) {
        throw await buildError(response);
    }
}

async function fetchText(url) {
    const response = await fetch(url, withAuthorization({ method: "GET" }));
    if (!response.ok) {
        throw await buildError(response);
    }
    return response.text();
}

async function fetchBlob(url) {
    const response = await fetch(url, withAuthorization({ method: "GET" }));
    if (!response.ok) {
        throw await buildError(response);
    }
    return response.blob();
}

function withAuthorization(options) {
    const headers = new Headers(options.headers || {});
    if (state.auth?.basic) {
        headers.set("Authorization", `Basic ${state.auth.basic}`);
    }
    return { ...options, headers };
}

async function buildError(response) {
    const contentType = response.headers.get("content-type") || "";
    let message = `Erro ${response.status}`;
    try {
        if (contentType.includes("application/json")) {
            const data = await response.json();
            message = data.message || data.error || JSON.stringify(data);
        } else {
            const text = await response.text();
            if (text) {
                message = text;
            }
        }
    } catch {
        message = `Erro ${response.status}`;
    }
    if (response.status === 401) {
        message = "Credenciais invalidas ou sessao expirada.";
    }
    if (response.status === 403) {
        message = "Seu perfil nao tem permissao para esta acao.";
    }
    return new Error(message);
}

function getErrorMessage(error) {
    return error instanceof Error ? error.message : "Falha inesperada.";
}