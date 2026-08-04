import { Routes } from '@angular/router';
import { ReservaListaComponent } from './components/reserva-lista/reserva-lista';
import { ReservaFormComponent } from './components/reserva-form/reserva-form';
import { LoginComponent } from './components/login/login';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/reservas', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'reservas', component: ReservaListaComponent, canActivate: [authGuard] },
  { path: 'reservas/nova', component: ReservaFormComponent, canActivate: [authGuard] },
  { path: 'reservas/editar/:id', component: ReservaFormComponent, canActivate: [authGuard] }
];
