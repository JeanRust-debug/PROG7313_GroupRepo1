package com.example.clearcash.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clearcash.R
import com.example.clearcash.databinding.FragmentLoginBinding

/**
 * Fragment for user login.
 * Validates credentials and navigates to the main app on success.
 * Author: Diya Maharaj ST10327888
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe login result — navigate to main app on success
        authViewModel.loginResult.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(requireContext(), "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                // Navigate to the main app (categoryFragment is the start destination)
                findNavController().navigate(R.id.action_loginFragment_to_categoryFragment)
                authViewModel.clearLoginResult()
            }
        }

        // Observe error messages
        authViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                authViewModel.clearError()
            }
        }

        // Login button click
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            authViewModel.login(username, password)
        }

        // Navigate to register screen
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}