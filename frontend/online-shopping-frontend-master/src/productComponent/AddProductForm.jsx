import { useState, useEffect } from "react";
import axios from "axios";
import { ToastContainer, toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

const AddProductForm = () => {
  const [categories, setCategories] = useState([]);

  let navigate = useNavigate();

  let adminToken = sessionStorage.getItem("admin-jwtToken");

  const retrieveAllCategories = async () => {
    const response = await axios.get(
      "http://localhost:8080/api/product/category/all"
    );
    return response.data;
  };

  useEffect(() => {
    const getAllCategories = async () => {
      const allCategories = await retrieveAllCategories();
      if (allCategories) {
        setCategories(allCategories.categories);
      }
    };

    getAllCategories();
  }, []);

  const [selectedPhoto, setSelectedPhoto] = useState(null);
  const [product, setProduct] = useState({
    title: "",
    description: "",
    price: "",
    quantity: "",
    categoryId: "",
  });

  const handleInput = (e) => {
    setProduct({ ...product, [e.target.name]: e.target.value });
  };

  const saveProduct = (e) => {
    e.preventDefault();
    if (product.categoryId === "" || product.categoryId === "0") {
      toast.error("Select Product Category", {
        position: "top-center",
        autoClose: 1000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
      });
    } else {
      const formData = new FormData();
      formData.append("image", selectedPhoto);
      formData.append("title", product.title);
      formData.append("description", product.description);
      formData.append("price", product.price);
      formData.append("quantity", product.quantity);
      formData.append("categoryId", product.categoryId);

      axios
        .post("http://localhost:8080/api/product/add", formData, {
          headers: {
            Authorization: `Bearer ${adminToken}`,
          },
        })
        .then((resp) => {
          let response = resp.data;

          if (response.success) {
            toast.success(response.responseMessage, {
              position: "top-center",
              autoClose: 1000,
              hideProgressBar: false,
              closeOnClick: true,
              pauseOnHover: true,
              draggable: true,
              progress: undefined,
            });

            setTimeout(() => {
              navigate("/home");
            }, 2000); // Redirect after 3 seconds
          } else if (!response.success) {
            toast.error(response.responseMessage, {
              position: "top-center",
              autoClose: 1000,
              hideProgressBar: false,
              closeOnClick: true,
              pauseOnHover: true,
              draggable: true,
              progress: undefined,
            });
            setTimeout(() => {
              window.location.reload(true);
            }, 2000); // Redirect after 3 seconds
          } else {
            toast.error("It Seems Server is down!!!", {
              position: "top-center",
              autoClose: 1000,
              hideProgressBar: false,
              closeOnClick: true,
              pauseOnHover: true,
              draggable: true,
              progress: undefined,
            });
            setTimeout(() => {
              window.location.reload(true);
            }, 2000); // Redirect after 3 seconds
          }
        })
        .catch((error) => {
          console.error(error);
          toast.error("It seems server is down", {
            position: "top-center",
            autoClose: 1000,
            hideProgressBar: false,
            closeOnClick: true,
            pauseOnHover: true,
            draggable: true,
            progress: undefined,
          });
          setTimeout(() => {
            window.location.reload(true);
          }, 1000); // Redirect after 3 seconds
        });
    }
  };

  return (
    <div>
      <div class="mt-2 d-flex aligns-items-center justify-content-center">
        <div
          class="card form-card border-color custom-bg"
          style={{ width: "50rem" }}
        >
          <div className="card-header bg-color custom-bg-text text-center">
            <h5 class="card-title">Add Product</h5>
          </div>
          <div class="card-body text-color">
            <form className="row g-3">
              <div class="col-md-6 mb-3">
                <label for="title" class="form-label">
                  <b>Product Title</b>
                </label>
                <input
                  type="text"
                  class="form-control"
                  id="title"
                  name="title"
                  onChange={handleInput}
                  value={product.title}
                  required
                />
              </div>
              <div class="col-md-6 mb-3">
                <label for="description" class="form-label">
                  <b>Product Description</b>
                </label>
                <textarea
                  class="form-control"
                  id="description"
                  name="description"
                  rows="3"
                  onChange={handleInput}
                  value={product.description}
                  required
                />
              </div>

              <div className="col-md-6 mb-3">
                <label className="form-label">
                  <b>Category</b>
                </label>

                <select
                  name="categoryId"
                  onChange={handleInput}
                  className="form-control"
                  required
                >
                  <option value="">Select Category</option>

                  {categories.map((category) => {
                    return (
                      <option value={category.id}> {category.title} </option>
                    );
                  })}
                </select>
              </div>

              <div class="col-md-6 mb-3">
                <label for="quantity" class="form-label">
                  <b>Product Quantity</b>
                </label>
                <input
                  type="number"
                  class="form-control"
                  id="quantity"
                  name="quantity"
                  onChange={handleInput}
                  value={product.quantity}
                  required
                />
              </div>

              <div class="col-md-6 mb-3">
                <label for="price" class="form-label">
                  <b>Product Price</b>
                </label>
                <input
                  type="number"
                  class="form-control"
                  id="price"
                  name="price"
                  onChange={handleInput}
                  value={product.price}
                  required
                />
              </div>

              <div class="col-md-6 mb-3">
                <label for="formFile" class="form-label">
                  <b> Select Product Image</b>
                </label>
                <input
                  class="form-control"
                  type="file"
                  id="formFile"
                  name="photo"
                  value={product.photo}
                  onChange={(e) => setSelectedPhoto(e.target.files[0])}
                  required
                />
              </div>
              <div className="d-flex aligns-items-center justify-content-center">
                <button
                  type="submit"
                  class="btn bg-color custom-bg-text"
                  onClick={saveProduct}
                >
                  Add Product
                </button>
                <ToastContainer />
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddProductForm;
