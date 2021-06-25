package com.cookandroid.instagramclone

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.AttributeSet
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchPeopleFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SearchPeopleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchPeopleFragment : Fragment() {
    val TAG = "serach fragment"
    var usersList = ArrayList<ProfileResponse>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        usersList.clear()
        return activity?.let {
            var v = inflater.inflate(R.layout.fragment_search_people, container, false)
            var recyclerView = v.findViewById<RecyclerView>(R.id.user_result_recycler_view)
            recyclerView.adapter = FindUserFragmentRecyclerViewAdapter()
            recyclerView.layoutManager = GridLayoutManager(container?.context,1)

            arguments?.let{
                it.getParcelableArrayList<ProfileResponse>("user")?.let{user -> usersList = user}
                usersList.forEach { profile->
                    Log.e(TAG, "version 2 ${profile.nickname}")
                }
            }
            recyclerView.adapter?.notifyDataSetChanged()
            v
        }
    }

    inner class FindUserFragmentRecyclerViewAdapter:
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels
            var height = resources.displayMetrics.heightPixels/10
            var view = LayoutInflater.from(parent.context).inflate(R.layout.user_find_result,parent,false)
            view.layoutParams = LinearLayout.LayoutParams(width,height)

            return UserFindResultViewHolder(view)
        }

        inner class UserFindResultViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            var displayIdTextView = view.findViewById<TextView>(R.id.profile_displayed_id)
            var IdTextView = view.findViewById<TextView>(R.id.profile_id)
            var profileImage = view.findViewById<ImageView>(R.id.user_find_profile_img)
            var fragment = GetUserPostActivity()
            init {
                fragment.arguments = Bundle(1)
                view.setOnClickListener {
                    activity?.let {
                        it.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_content, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
                profileImage.setImageResource(R.drawable.ic_account)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var h = holder as UserFindResultViewHolder
            h.fragment.arguments?.putParcelable("user", usersList[position])
            h.displayIdTextView.text = usersList[position].nickname
            h.IdTextView.text = usersList[position].displayId
        }

        override fun getItemCount(): Int {
            return usersList.size
        }
    }
}
