import React, { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "./ui/dialog";
import { Button } from "./ui/button";
import { DialogDescription } from "@radix-ui/react-dialog";
import { Label } from "./ui/label";
import { Input } from "./ui/input";
import { signup, login } from "../api";
import { setUser } from "@/store";
import { useDispatch } from "react-redux";
const SignupDialog = ({ trigger }: { trigger: React.ReactNode }) => {
  const [isSignup, setIsSignup] = useState(true);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [adminSignupToken, setAdminSignupToken] = useState("");
  const [open, setopem] = useState(false);
  const [error, setError] = useState("");
  const dispatch = useDispatch();
  const handleAuth = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    if (isSignup) {
      try {
        const signin = await signup(
          firstName,
          lastName,
          email,
          phoneNumber,
          password,
          adminSignupToken
        );
        dispatch(setUser(signin));
        setopem(false);
        clearform();
      } catch {
        setError("Could not create your account. Check your details and try again.");
      }
    } else {
      try {
        const data = await login(email, password);
        dispatch(setUser(data));
        setopem(false);
        clearform();
      } catch (error) {
        const status = (error as { response?: { status?: number } })?.response?.status;
        setError(status === 401 ? "Email or password is incorrect." : "Could not sign in. Please try again.");
      }
    }
  };
  const clearform = () => {
    setFirstName("");
    setLastName("");
    setEmail("");
    setPassword("");
    setPhoneNumber("");
    setAdminSignupToken("");
  };
  return (
    <Dialog open={open} onOpenChange={setopem}>
      <DialogTrigger asChild>
        {trigger}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px] bg-white">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold">
            {isSignup ? "Create Account" : "Welcome Back"}
          </DialogTitle>
          <DialogDescription>
            {isSignup
              ? "Join us to start booking your travels."
              : "Enter your credentials to access your account."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleAuth} className="space-y-4 py-4">
          {isSignup && (
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input
                  id="firstName"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  id="lastName"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </div>
            </div>
          )}
          {error && <p role="alert" className="text-sm text-red-700">{error}</p>}
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          {isSignup && (
            <div className="space-y-2">
              <Label htmlFor="phoneNumber">Phone Number</Label>
              <Input
                id="phoneNumber"
                type="tel"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                required
              />
            </div>
          )}
          {isSignup && (
            <div className="space-y-2">
              <Label htmlFor="adminSignupToken">Admin invite token (optional)</Label>
              <Input id="adminSignupToken" autoComplete="off" value={adminSignupToken} onChange={(e) => setAdminSignupToken(e.target.value)} />
              <p className="text-xs text-slate-500">Leave blank for a standard account. Paste only the code starting with MMT-ADM- (omit its list number). Each invite works once.</p>
            </div>
          )}
          <Button
            type="submit"
            className="w-full bg-blue-600 text-white"
            variant="outline"
          >
            {isSignup ? "Sign Up" : "Login"}
          </Button>
        </form>
        <div className="text-center text-sm">
          {isSignup ? (
            <>
              Already have an account?{" "}
              <Button
                variant="link"
                className="p-0 text-blue-600"
                onClick={() => setIsSignup(false)}
              >
                Login
              </Button>
            </>
          ) : (
            <>
              Don&apos;t have an account?{" "}
              <Button
                variant="link"
                className="p-0 text-blue-600"
                onClick={() => setIsSignup(true)}
              >
                Sign Up
              </Button>
            </>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default SignupDialog;
