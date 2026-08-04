import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

export interface Credenciais {
  usuario: string;
  senha: string;
}

// Usuários permitidos (frontend-only — sem backend)
const USUARIOS: Credenciais[] = [
  { usuario: 'admin', senha: 'admin123' },
  { usuario: 'recepcao', senha: 'recepcao123' }
];

const STORAGE_KEY = 'auth_usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private router: Router) {}

  login(usuario: string, senha: string): boolean {
    const encontrado = USUARIOS.find(
      u => u.usuario === usuario.trim().toLowerCase() && u.senha === senha
    );
    if (encontrado) {
      localStorage.setItem(STORAGE_KEY, encontrado.usuario);
      return true;
    }
    return false;
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(STORAGE_KEY);
  }

  getUsuario(): string {
    return localStorage.getItem(STORAGE_KEY) ?? '';
  }
}
