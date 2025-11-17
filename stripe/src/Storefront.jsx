import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const Product = ({ name, price, priceId, period, image, accountId }) => {
  const handleCheckout = async (e) => {
    e.preventDefault();
    
    console.log("Creating checkout with:", { priceId, accountId });
    
    try {
      const response = await fetch("/api/create-checkout-session", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          priceId: priceId,
          accountId: accountId,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        console.error("Checkout error:", errorData);
        alert("Error creating checkout session. Check console for details.");
        return;
      }

      const data = await response.json();
      console.log("Checkout response:", data);
      
      // Redirect to Stripe Checkout
      if (data.url) {
        window.location.href = data.url;
      } else {
        console.error("No checkout URL returned:", data);
        alert("Error: No checkout URL received");
      }
    } catch (error) {
      console.error("Error:", error);
      alert("Error creating checkout session");
    }
  };

  return (
    <div className="product round-border">
      <div className="product-info">
        <img src={image} alt={name} />
        <div className="description">
          <h3>{name}</h3>
          <h5>${(price / 100).toFixed(2)} {period && `/ ${period}`}</h5>
          <small style={{color: '#666'}}>Price ID: {priceId}</small>
        </div>
      </div>

      <form onSubmit={handleCheckout}>
        <button className="button" type="submit">
          Buy Now
        </button>
      </form>
    </div>
  );
};

const Storefront = () => {
  const { accountId } = useParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchProducts = async () => {
    if (!accountId) return;
    
    try {
      setLoading(true);
      const response = await fetch(`/api/products/${accountId}`);
      const data = await response.json();
      setProducts(data);
    } catch (error) {
      console.error("Error fetching products:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [accountId]);

  return (
    <div className="App">
      <div className="container">
        <div className="logo">
          {accountId === "platform"
            ? "Platform Products"
            : `Store ${accountId}`}
        </div>
        
        {loading ? (
          <p>Loading products...</p>
        ) : products.length === 0 ? (
          <div style={{ marginTop: "20px" }}>
            <p>No products available yet.</p>
            <p>Create products from the home page first!</p>
          </div>
        ) : (
          <div>
            {products.map((product) => (
              <Product key={product.id} {...product} accountId={accountId} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Storefront;