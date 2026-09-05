import React, { useState } from 'react';

export function DemoCheckout() {
  const [amount, setAmount] = useState<number>(5000);
  const [email, setEmail] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handlePayment = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/demo/create-order`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-KEY': import.meta.env.VITE_API_KEY
        },
        body: JSON.stringify({
          amount,
          email: email.trim() || undefined,
          phone: phone.trim() || undefined,
        })
      });

      if (!res.ok) {
        throw new Error('Failed to create order');
      }

      const order = await res.json();

      const options = {
        key: import.meta.env.VITE_RAZORPAY_KEY_ID,
        amount: order.amount,
        currency: order.currency,
        name: "RecoverAI Demo",
        description: "Test Transaction",
        image: "/favicon.svg",
        order_id: order.id,
        handler: function (response: any) {
          alert(`Payment Successful! Payment ID: ${response.razorpay_payment_id}`);
        },
        prefill: {
          name: "Test Customer",
          email: email.trim() || "customer@example.com",
          contact: phone.trim() || "9999999999"
        },
        theme: {
          color: "#0f172a"
        }
      };

      const rzp1 = new (window as any).Razorpay(options);
      rzp1.on('payment.failed', function (response: any) {
        alert(`Payment Failed. Reason: ${response.error.description}`);
      });
      rzp1.open();
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-page flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-border-default p-8 space-y-8">
        <div className="text-center">
          <h1 className="text-2xl font-display font-semibold text-text-primary">Demo Checkout</h1>
          <p className="text-text-secondary mt-2">Create a test transaction for RecoverAI.</p>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">Amount (₹)</label>
            <input
              type="number"
              min="100"
              step="100"
              value={amount}
              onChange={(e) => setAmount(Number(e.target.value))}
              className="w-full px-4 py-2 border border-border-default rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-blue"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">
              Email <span className="text-text-secondary font-normal">(optional — needed for live notification demo)</span>
            </label>
            <input
              type="email"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 border border-border-default rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-blue"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-text-primary mb-1">
              Phone <span className="text-text-secondary font-normal">(optional — needed for SMS/WhatsApp demo)</span>
            </label>
            <input
              type="tel"
              placeholder="9999999999"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full px-4 py-2 border border-border-default rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-blue"
            />
          </div>

          {(email.trim() || phone.trim()) && (
            <div className="p-3 bg-green-50 border border-green-200 text-green-700 rounded-lg text-sm">
              ✓ Contact info captured — a real notification will be dispatched if a payment failure is recovered.
            </div>
          )}

          {error && (
            <div className="p-3 bg-semantic-red/10 text-semantic-red rounded-lg text-sm">
              {error}
            </div>
          )}

          <button
            onClick={handlePayment}
            disabled={loading}
            className="w-full py-3 bg-accent-blue text-white font-medium rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50"
          >
            {loading ? 'Processing...' : `Pay ₹${amount}`}
          </button>
        </div>

        <details className="text-sm text-text-secondary border-t border-border-default pt-4">
          <summary className="cursor-pointer font-medium hover:text-text-primary">View Test Credentials</summary>
          <div className="mt-4 space-y-4 font-mono text-xs">
            <div>
              <ul className="list-disc pl-4 space-y-1">
                <li>Card: <span className="bg-page px-1 rounded">4100 2800 0009 0000</span></li>
              </ul>
            </div>
          </div>
        </details>
      </div>
    </div>
  );
}
