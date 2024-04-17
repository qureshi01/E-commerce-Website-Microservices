import { useState } from "react";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { useNavigate } from "react-router-dom";

const ForgetPassword = () => {
  let navigate = useNavigate();

  const [forgetRequest, setForgetRequest] = useState({
    emailId: "",
    password: "",
    newPassword: "",
    confirmPassword: "",
  });

  const handleUserInput = (e) => {
    setForgetRequest({ ...forgetRequest, [e.target.name]: e.target.value });
  };

  const changePassword = (e) => {
    if (forgetRequest.confirmPassword !== forgetRequest.newPassword) {
      toast.error("Password Not Matching", {
        position: "top-center",
        autoClose: 1000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
      });
    } else {
      fetch("http://localhost:8080/api/user/forget-password", {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify(forgetRequest),
      })
        .then((result) => {
          console.log("result", result);
          result.json().then((res) => {
            if (res.success) {
              toast.success(res.responseMessage, {
                position: "top-center",
                autoClose: 1000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                progress: undefined,
              });
              setTimeout(() => {
                window.location.href = "/home";
              }, 1000); // Redirect after 3 seconds
            } else {
              toast.error(res.responseMessage, {
                position: "top-center",
                autoClose: 1000,
                hideProgressBar: false,
                closeOnClick: true,
                pauseOnHover: true,
                draggable: true,
                progress: undefined,
              });
            }
          });
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
        });
    }

    e.preventDefault();
  };

  return (
    <div>
      <div className="mt-2 d-flex aligns-items-center justify-content-center">
        <div
          className="card form-card border-color custom-bg"
          style={{ width: "25rem" }}
        >
          <div className="card-header bg-color text-center custom-bg-text">
            <h4 className="card-title">Forget Password</h4>
          </div>
          <div className="card-body">
            <form>
              <div className="mb-3 text-color">
                <label for="emailId" class="form-label">
                  <b>Email Id</b>
                </label>
                <input
                  type="email"
                  className="form-control"
                  id="emailId"
                  name="emailId"
                  onChange={handleUserInput}
                  value={forgetRequest.emailId}
                  required
                />
              </div>
              <div className="mb-3 text-color">
                <label for="password" className="form-label">
                  <b>Password</b>
                </label>
                <input
                  type="password"
                  className="form-control"
                  id="password"
                  name="password"
                  onChange={handleUserInput}
                  value={forgetRequest.password}
                  required
                />
              </div>
              <div className="mb-3 text-color">
                <label for="password" className="form-label">
                  <b>New Password</b>
                </label>
                <input
                  type="password"
                  className="form-control"
                  id="newPassword"
                  name="newPassword"
                  onChange={handleUserInput}
                  value={forgetRequest.newPassword}
                  required
                />
              </div>
              <div className="mb-3 text-color">
                <label for="password" className="form-label">
                  <b>Confirm Password</b>
                </label>
                <input
                  type="password"
                  className="form-control"
                  id="confirmPassword"
                  name="confirmPassword"
                  onChange={handleUserInput}
                  value={forgetRequest.confirmPassword}
                  required
                />
              </div>

              <button
                type="submit"
                className="btn bg-color custom-bg-text"
                onClick={changePassword}
              >
                Change Password
              </button>
              <ToastContainer />
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgetPassword;
