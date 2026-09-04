import { createClient } from '@supabase/supabase-js';

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);
const json = (body, status = 200) => ({ status, headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': process.env.FRONTEND_ORIGIN || '*' }, body: JSON.stringify(body) });
const error = (message, status = 400) => json({ error: message }, status);
const authUser = async (req) => {
  const token = req.headers.authorization?.replace(/^Bearer\s+/i, '');
  if (!token) return null;
  const { data } = await supabase.auth.getUser(token);
  return data.user || null;
};
const admin = async (req) => {
  const user = await authUser(req);
  if (!user) return null;
  const { data } = await supabase.from('profiles').select('role').eq('id', user.id).single();
  return data?.role === 'ADMIN' ? user : null;
};
const ticketView = (ticket) => ({ code: ticket.code, qrToken: ticket.qr_token, customer: ticket.profiles?.full_name || '', price: Number(ticket.price), status: ticket.status, purchasedAt: ticket.purchased_at.replace(/Z$/, ''), expiresAt: ticket.expires_at.replace(/Z$/, '') });
const ticketQuery = 'code,qr_token,price,status,purchased_at,expires_at,profiles!tickets_customer_id_fkey(full_name)';
const salesWindow = () => {
  const now = new Date();
  const day = now.getUTCDay();
  const hour = now.getUTCHours();
  const open = day === 4 || (day === 5 && hour < 10);
  const boundary = new Date(now);
  if (open) {
    boundary.setUTCDate(now.getUTCDate() + (day === 4 ? 1 : 0));
    boundary.setUTCHours(day === 4 ? 0 : 10, 0, 0, 0);
  } else {
    const daysUntilThursday = (4 - day + 7) % 7 || 7;
    boundary.setUTCDate(now.getUTCDate() + daysUntilThursday);
    boundary.setUTCHours(0, 0, 0, 0);
  }
  return { open, nextBoundary: boundary.toISOString() };
};

export default async function handler(req, res) {
  if (req.method === 'OPTIONS') return res.status(204).end();
  const path = req.url.split('?')[0].replace(/^\/api\/?/, '').split('/');
  try {
    if (path[0] === 'sales-status' && req.method === 'GET') {
      const testingMode = process.env.TESTING_MODE === 'true';
      const window = salesWindow();
      return res.status(200).json({ open: testingMode || window.open, testingMode, nextBoundary: window.nextBoundary, timezone: 'Africa/Dakar' });
    }
    if (path[0] === 'auth' && path[1] === 'login' && req.method === 'POST') {
      const { email, password } = req.body || {};
      const { data, error: loginError } = await supabase.auth.signInWithPassword({ email: email?.trim().toLowerCase(), password });
      if (loginError) return res.status(401).json({ error: 'Identifiants invalides.' });
      return res.status(200).json({ token: data.session.access_token });
    }
    if (path[0] === 'tickets' && path[1] === 'acheter' && req.method === 'POST') {
      const { fullName, phone, email, quantity } = req.body || {};
      if (!fullName || !phone || !email || !Number.isInteger(quantity) || quantity < 1 || quantity > 10) return res.status(400).json({ error: 'Données d’achat invalides.' });
      const { data, error: purchaseError } = await supabase.rpc('create_purchase', { p_full_name: fullName.trim(), p_phone: phone.trim(), p_email: email.trim().toLowerCase(), p_quantity: quantity });
      if (purchaseError) return res.status(400).json({ error: purchaseError.message });
      return res.status(200).json(data.map(ticketView));
    }
    if (path[0] === 'tickets' && path[1] && req.method === 'GET') {
      const { data, error: lookupError } = await supabase.from('tickets').select(ticketQuery).or(`code.eq.${path[1]},qr_token.eq.${path[1]}`).maybeSingle();
      if (lookupError || !data) return res.status(404).json({ error: 'Ticket introuvable.' });
      return res.status(200).json(ticketView(data));
    }
    if (path[0] === 'tickets' && path[1] === 'verifier' && req.method === 'POST') {
      if (!await admin(req)) return res.status(401).json({ error: 'Authentification requise.' });
      const { data, error: verifyError } = await supabase.rpc('verify_ticket', { p_code: req.body?.code });
      if (verifyError || !data?.length) return res.status(404).json({ error: verifyError?.message || 'Ticket introuvable.' });
      return res.status(200).json(ticketView(data[0]));
    }
    if (path[0] === 'admin' && await admin(req)) {
      if (path[1] === 'dashboard' && req.method === 'GET') {
        const { data, error: dashboardError } = await supabase.rpc('dashboard_stats');
        if (dashboardError) return error(dashboardError.message, 500);
        return res.status(200).json(data);
      }
      if (path[1] === 'tickets' && req.method === 'GET') {
        const { data, error: listError } = await supabase.from('tickets').select(ticketQuery).order('purchased_at', { ascending: false }).limit(100);
        if (listError) return error(listError.message, 500);
        return res.status(200).json(data.map(ticketView));
      }
      if (path[1] === 'tickets' && path[2] && req.method === 'PATCH') {
        const { error: updateError } = await supabase.from('tickets').update({ status: req.body?.status }).eq('code', path[2]);
        if (updateError) return error(updateError.message);
        return res.status(204).end();
      }
    }
    return res.status(404).json({ error: 'Route introuvable.' });
  } catch (e) { return res.status(500).json({ error: e.message || 'Erreur serveur.' }); }
}
