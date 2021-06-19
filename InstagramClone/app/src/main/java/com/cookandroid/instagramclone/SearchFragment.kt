package com.cookandroid.instagramclone

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    val TAG = "search"
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        var searchView = view.findViewById<SearchView>(R.id.search_bar)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                p0?.let {findId->
                    Log.d("search", "id $findId")
                    var retrofit = InternetCommunication.getRetrofitGson()
                    var retrofitService = retrofit.create(ProfileController::class.java)
                    var service = retrofitService?.getUserProfile(findId)

                    service?.enqueue(object : Callback<ProfileResponse> {
                        override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                            Log.d(TAG, "get profile failed")
                        }

                        override fun onResponse(
                            call: Call<ProfileResponse>,
                            response: Response<ProfileResponse>
                        ) {
                            var bundle = Bundle(1)
                            var message = when(response.code()){
                                200-> {
                                    var member = response.body()
                                    member?.let{ m->
                                        val info = m.memberDto
                                        info?.let{
                                            Log.e(TAG, "nickname ${info.nickname}")
                                            Log.e(TAG, "displayId ${info.displayId}")
                                            Log.e(TAG, "profileImageUrl ${info.profileImageUrl}")
                                            Log.e(TAG, "introduction ${info.introduction}")
                                            Log.e(TAG, "isFollow ${info.isFollow}")
                                            Log.e(TAG, "post count ${m.postCount}")
                                            Log.e(TAG, "follower Count ${m.followerCount}")
                                            Log.e(TAG, "following Count ${m.followingCount}")
                                        } ?: Log.e(TAG, "member dto is null")

                                        var usersList = ArrayList<ProfileResponse>()
                                        usersList.add(m)
                                        bundle.putParcelableArrayList("user", usersList)
                                        var fragment = SearchPeopleFragment(childFragmentManager)
                                        fragment.arguments = bundle

                                        childFragmentManager.beginTransaction().replace(R.id.search_result, fragment).commit()
                                    }
                                    "Success"
                                }
                                else-> {
                                    Log.e(TAG, response.body().toString())
                                    var fragment = SearchPeopleFragment(childFragmentManager)

                                    childFragmentManager.beginTransaction().replace(R.id.search_result, fragment).commit()
                                    "unknown error"
                                }
                            }
                            Log.e(TAG, message)
                        }
                    })
                }
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
