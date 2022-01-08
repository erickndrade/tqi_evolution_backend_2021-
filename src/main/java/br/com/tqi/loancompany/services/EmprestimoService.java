package br.com.tqi.loancompany.services;

import br.com.tqi.loancompany.domain.Cliente;
import br.com.tqi.loancompany.domain.Emprestimo;
import br.com.tqi.loancompany.exceptions.BusinessException;
import br.com.tqi.loancompany.exceptions.ObjectNotFoundException;
import br.com.tqi.loancompany.repository.ClienteRepository;
import br.com.tqi.loancompany.repository.EmprestimoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    public void validarPrazo(Emprestimo emprestimo) {
        LocalDate primeiraParcela = emprestimo.getPrimeiraParcela();
        LocalDate dataPagamento = LocalDate.now().plusMonths(3);
        if (primeiraParcela.isAfter(dataPagamento)) {
            throw new BusinessException("Prazo Inválido! Limite: 90 dias.");
        }
    }

    public void validarParcela(Emprestimo emprestimo) {
        Integer quantidadeParcelas = emprestimo.getQuantidadeParcelas();
        if (quantidadeParcelas > 60) {
            throw new BusinessException("Quantidade de parcelas inválido. Limite: 60");
        }
    }

    public List<Emprestimo> findAll() {
        return emprestimoRepository.findAll();
    }

    public Emprestimo inserirEmprestimo(Emprestimo emprestimo, Integer clienteId) {
        Cliente cliente = clienteRepository.getById(clienteId);
        emprestimo.setCliente(cliente);
        validarPrazo(emprestimo);
        validarParcela(emprestimo);
        return emprestimoRepository.save(emprestimo);
    }

    public Emprestimo findByCliente(Integer id) {
        Emprestimo emprestimo = emprestimoRepository.findClienteById(id);
        if (emprestimo == null) {
            throw new ObjectNotFoundException("Empréstimo não encontrado para o cliente informado!");
        }
        return emprestimo;
    }

    public Emprestimo findById(Integer id) {
        Optional<Emprestimo> emprestimo = emprestimoRepository.findById(id);
        if (emprestimo.isPresent()) {
            return emprestimo.get();
        }
        throw new ObjectNotFoundException("Emprestimo não encontrado!");
    }

    public void deleteById(Integer id) {
        findById(id);
        try {
            emprestimoRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            throw new DataIntegrityViolationException("Não foi possível excluir este empréstimo!");
        }
    }

    public Emprestimo replace(Integer id, Emprestimo emprestimo) {
        emprestimo.setId(id);
        return emprestimoRepository.save(emprestimo);
    }
}